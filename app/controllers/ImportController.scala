package controllers

import batch.utils._
import model._
import play.api.libs.json.Json
import play.api.mvc.Controller
import services._
import utils.oAuthActions.AdminAuthAction
import utils.{LoggerAudit, PasswordGenerator}

import scala.util.{Failure, Success, Try}


/**
  * Created by fsznajderman on 07/02/2017.
  */
class ImportController(personService: PersonService
                       , sponsorService: SponsorService
                       , eventService: EventService
                       , remoteClient: RemoteClient
                       , notificationService: NotificationService) extends Controller with LoggerAudit {

  case class RepresentativeMapping(regId: String, email: String, sponsor: String, sponsorLevel: String)

  def importAllRepresentatives() = AdminAuthAction(parse.multipartFormData) { implicit request =>

    val body = request.body
    body.file("csvFile").foreach { csvFile =>

      val csv = csvFile.ref
      val r: Seq[Map[String, String]] = loadCSVSourceFileWithLib(csv.file)
      // emails are clean from Devoxx FR
      val representatives: Seq[RepresentativeMapping] = for {
        kv <- r
      } yield RepresentativeMapping(
        kv.getOrElse("RegId", "No registrantId")
        , kv.getOrElse("Email_Address", "Email address not found")
        , kv.getOrElse("Company", "Company not found")
        , kv.getOrElse("Ticket_family", "unknown_sponsor_level")
      )

      representatives.foreach {
        representative =>
          val sponsor = sponsorService.loadSponsor(representative.sponsor) match {
            case None =>
              eventService.addEvent(Event(typeEvent = ImportRepresentative.typeEvent, message = s"Sponsor with name ${representative.sponsor} not found, creating a new sponsor"))
              val newSponsor = Sponsor(None, representative.sponsor, representative.sponsorLevel)
              sponsorService.addSponsor(newSponsor)
              play.Logger.debug(s"Created new sponsor [${newSponsor.name}]")
              sponsorService.loadSponsor(representative.sponsor).get // TODO pas top de devoir recharger ce que l'om vient de créer mais c'est pour récupérer le bon sponsorId
            case Some(exSponsor) =>
              play.Logger.debug(s"Loaded sponsor ${exSponsor.name} with id ${exSponsor.id.get}")
              exSponsor
          }

          personService.getCompletePerson(representative.regId) match {
            case None =>
              eventService.addEvent(Event(typeEvent = ImportRepresentative.typeEvent, message = s"Person not found for Representative regId : ${representative.regId}"))
              // If the Person does not exist then we will not create it here
              // It means that the representatives must already exist in the Database as Person
              play.Logger.warn(s"A Person was not found while trying to import representatives. Please import this person first. $representative")
            case Some(person) => {
              if (sponsorService.isRepresentative(person.regId, sponsor.id.get)) {
                play.Logger.info("Already representative")
                // Maybe update ?
                eventService.addEvent(
                  Event(
                    typeEvent = ImportRepresentative.typeEvent,
                    message = s"Representative already exists ${person.email} ${person.regId} for ${sponsor.name}")
                )
              } else {
                play.Logger.info("New representative")
                Try {
                  sponsorService.addRepresentative(person.regId, sponsor.id.get)
                } match {
                  case Success(_) => {
                    val currentToken = jsonUtils.tokenExtractorFromSession(request)
                    val pass = PasswordGenerator.generatePassword
                    remoteClient.sendPassword(person.regId, pass, currentToken) // Update the password on MyDevoxx... maybe not a good idea if the user does already exist
                    notificationService.sendMail(Seq(person.email),
                      Option(views.txt.mails.notifPassword.render(person.firstname, sponsor.name, pass, person.email).body),
                      Option(views.html.mails.notifPassword.render(person.firstname, sponsor.name, pass, person.email).body))
                    eventService.addEvent(Event(typeEvent = ImportRepresentative.typeEvent, message = s"Email sent to ${person.firstname} ${person.lastname} (${person.email}) for sponsor ${sponsor.name}"))
                  }
                  case Failure(e) =>
                    eventService.addEvent(Event(typeEvent = AddRepresentative.typeEvent, message = s"An error occured when addRepresentative ${e.getMessage}"))

                }
              }
            }
          }


      }
    }
    Ok("Representatives imported")
  }


  def importAllAttendees() = AdminAuthAction(parse.multipartFormData) { implicit request =>
    val body = request.body
    body.file("csvFile").map { csvFile =>
      val csv = csvFile.ref
      val r: Seq[Map[String, String]] = loadCSVSourceFileWithLib(csv.file)
      val convertedPerson = for {
        kv <- r
      } yield Person(kv.get("RegId"), Json.toJson(kv).toString)

      val currentToken = jsonUtils.tokenExtractorFromSession(request)
      import Person._
      val imported = convertedPerson.map { p =>
        //notify MyDevoxx with new person
        Try {
          remoteClient.sendPerson(Json.parse(p.json).as[PersonJson], currentToken)
          personService.addPerson(p)
        }
        match {
          case Failure(e) =>
            logger.error(p.json + "  " + e.getMessage)
            0
          case _ => 1
        }
      }
      Ok(s"Imported ${imported.sum} persons, with ${convertedPerson.size} CSV lines")
    }.getOrElse(NotFound("No file received"))

  }

  def importIndex = AdminAuthAction {
    Ok(views.html.importData())
  }
}


