package controllers

import batch.utils._
import model._
import play.api.libs.json.Json
import play.api.mvc.Controller
import services._
import utils.oAuthActions.AdminAuthAction
import utils.{LoggerAudit, PasswordGenerator}

import scala.util.{Failure, Try}

/**
  * Created by fsznajderman on 07/02/2017.
  */
class ImportController(ps: PersonService, ss: SponsorService, es: EventService, remote: RemoteClient, ns: NotificationService) extends Controller with LoggerAudit {

  case class representativeMapping(email: String, sponsor: String)

  def importAllRepresentatives() = AdminAuthAction(parse.multipartFormData) { implicit request =>

    val body = request.body
    body.file("csvFile").foreach { csvFile =>

      val csv = csvFile.ref
      val r: Seq[Map[String, String]] = loadCVSSourceFile(csv.file).map(kv => kv.updated("Email", kv.getOrElse("Email", "notFound").toLowerCase()))


      val reprs = for {
        kv <- r
      } yield representativeMapping(kv.getOrElse("Email", "notFound"), kv.getOrElse("sponsor", "notFound"))


      //Load item to avoid query data several times
      val sponsors = ss.loadSponsors()
      val persons = ps.getAllCompletePerson

      reprs.foreach { repre =>
        sponsors.find(s => repre.sponsor.toLowerCase == s.name.toLowerCase) match {

          case None => {
            es.addEvent(Event(typeEvent = ImportRepresentative.typeEvent, message = s"Sponsor with name : ${repre.sponsor} not found")
            )
          }
          case Some(sp) => {
            persons.find(p => p.email.toLowerCase == repre.email.toLowerCase) match {
              case None =>
                es.addEvent(Event(typeEvent = ImportRepresentative.typeEvent, message = s"representative with email : ${repre.email} not found"))
              case Some(person) => {
                ss.addRepresentative(person.regId.toLong, sp.id.get)

                val currentToken = jsonUtils.tokenExtractorFromSession(request)
                val pass = PasswordGenerator.generatePassword

                remote.sendPassword(person.regId.toLong, pass, currentToken)
                ns.sendMail(Seq(person.email),
                  Option(views.txt.mails.notifPassword.render(person.firstname, sp.name, pass).body),
                  Option(views.html.mails.notifPassword.render(person.firstname, sp.name, pass).body))
                es.addEvent(Event(typeEvent = ImportRepresentative.typeEvent, message = s"Email sent to ${person.firstname} ${person.lastname} (${person.email}) for sponsor ${sp.name}"))
              }
            }
          }
        }

      }
    }


    Ok("Representative are imported")

  }


  def importAllAttendees() = AdminAuthAction(parse.multipartFormData) { implicit request =>
    val body = request.body
    body.file("csvFile").map { csvFile =>

      val csv = csvFile.ref
      val r: Seq[Map[String, String]] = loadCVSSourceFile(csv.file).map(kv => kv.updated("Email_Address", kv.getOrElse("Email_Address", "notFound").toLowerCase()))
      val convertedPerson = for {
        kv <- r
      } yield Person(kv.get("RegId").map(_.toLong),
        kv.getOrElse("first_Name", "notFound"),
        kv.getOrElse("last_Name", "notFound"),
        kv.getOrElse("gender", "_"),
        kv.getOrElse("Title", "notFound"),
        kv.getOrElse("status", "na"),
        2,
        isTraining = false,
        showSensitive = true,
        1,
        Json.toJson(kv).toString

      )

      val convertedPersonSensitive = for {
        kv <- r
      } yield PersonSensitive(kv.get("RegId").map(_.toLong),
        kv.getOrElse("Email_Address", "notFound"),
        kv.getOrElse("Phone", "notFound"),
        kv.getOrElse("Company", "notFound"),
        kv.getOrElse("City", "notFound"),
        lookingForAJob = false
      )


      val currentToken = jsonUtils.tokenExtractorFromSession(request)
      import Person._
      convertedPerson.foreach { p =>

        //notify MyDevoxx with new person

        Try {
          remote.sendPerson(Json.parse(p.json).as[PersonJson], currentToken)

          val token = jsonUtils.tokenExtractorFromSession(request)
          ps.addPerson(p, token)

        }
        match {
          case Failure(e) => logger.error(p.json + "  " + e.getMessage)
          case _ =>
        }
      }
      convertedPersonSensitive.foreach(ps.addPersonSensitive)

    }

    Ok("import done!")
  }

  def importIndex = AdminAuthAction {
    Ok(views.html.importData())
  }
}


