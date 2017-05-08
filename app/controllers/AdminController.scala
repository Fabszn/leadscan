package controllers

import java.io.{File, FileOutputStream, FileWriter, OutputStreamWriter}
import java.nio.charset.Charset
import java.time.LocalDateTime

import com.opencsv.CSVWriter
import model.{ErrorMessage, Event, PersonJson}
import org.apache.commons.lang3.{RandomStringUtils, StringUtils}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Reads, _}
import play.api.mvc.{Action, Controller}
import repository.LeadDAO.Item
import services._
import utils.HateoasUtils.toHateoas
import utils.oAuthActions.{AdminAuthAction, AdminRootAction}
import utils.{LoggerAudit, PasswordGenerator}

/**
  * Created by fsznajderman on 10/02/2017.
  */
class AdminController(ps: PersonService, ss: SponsorService, sts: StatsService, ns: NotificationService, sys: SyncService, remote: RemoteClient, es: EventService) extends Controller with LoggerAudit {


  def index = AdminRootAction {
    Ok(views.html.reports.reports())
  }

  def admin = AdminRootAction {
    Ok(views.html.admin.admin())
  }

  def person = AdminAuthAction {
    Ok(views.html.admin.person())
  }

  def stats = AdminAuthAction {
    Ok(views.html.admin.stats())
  }

  def export = AdminAuthAction {

    Ok(views.html.admin.export())
  }

  def repreSpnsor = AdminAuthAction {

    Ok(views.html.admin.onlyReprentative())
  }

  def passView = AdminAuthAction {
    Ok(views.html.admin.pass())
  }

  def eventView = AdminAuthAction {
    Ok(views.html.admin.events())
  }

  def statsData = AdminAuthAction {
    val points = sts.getData.leadsDateTime.map(i => JsNumber(Item.tupleFormated(i)._1))
    val dataTime = sts.getData.leadsDateTime.map(i => JsString(Item.tupleFormated(i)._2))
    val nbLead = sts.getData.sponsorStat.map(i => JsNumber(i._1))
    val sponsors = sts.getData.sponsorStat.map(i => JsString(i._2))

    Ok(Json.toJson(Map("points" -> points, "datetime" -> dataTime, "nbLead" -> nbLead, "sponsors" -> sponsors)))
  }


  def all() = AdminAuthAction {

    Ok(Json.toJson(Map("data" -> ps.allPersons().map(p => Seq(p.id.get.toString, "toBecompleted", "toBecompleted")))))
  }


  //def loadRepresentative

  def linkRepreSponsor = AdminAuthAction(parse.json) { implicit request =>

    case class RepresentativeSponsor(idPerson: String, idSponsor: Long)

    implicit val repSpoReaader: Reads[RepresentativeSponsor] = (
      (__ \ "idPerson").read[String] and (__ \ "idSponsor").read[Long]
      ) (RepresentativeSponsor.apply _)


    request.body.validate[RepresentativeSponsor].asEither match {
      case Right(link) => {
        ss.addRepresentative(link.idPerson, link.idSponsor)
        //val token = jsonUtils.tokenExtractorFromSession(request)

        for {
          p <- ps.getCompletePerson(link.idPerson)
          s <- ss.loadSponsor(link.idSponsor)
        } yield {
          import scala.concurrent.ExecutionContext.Implicits.global
          val pass = PasswordGenerator.generatePassword
          ps.addpass(p.regId, pass)
          remote.sendPassword(p.regId, pass).foreach { _ =>
            ns.sendMail(
              Seq(p.email),
              Option(views.txt.mails.notifPassword.render(p.firstname, s.name, pass, p.email, "").body),
              Option(views.html.mails.notifPassword.render(p.firstname, s.name, pass, p.email, "").body)
            )
          }
        }
        Created("Representative and sponsor are associated")

      }
      case Left(errors) => BadRequest(toHateoas(ErrorMessage("Json_parsing_error", s"Json parsing throws an error $errors")))
    }

  }


  def newPerson = AdminAuthAction(parse.json) { implicit request =>
    case class NewPerson(firstname: String, lastname: String, email: String, company: String, title: String)

    implicit val newPersonReaader: Reads[NewPerson] = (
      (__ \ "firstname").read[String] and (__ \ "lastname").read[String] and (__ \ "email").read[String] and (__ \ "company").read[String] and (__ \ "title").read[String]
      ) (NewPerson.apply _)


    request.body.validate[NewPerson].asEither match {
      case Right(p) => {
        val pj = ps.addRepresentative(p.firstname, p.lastname, p.email.toLowerCase().trim, p.company, p.title)
        //send person to Mydevoxx
        remote.sendPerson(pj)

        Created("representative has been created")
      }
      case Left(errors) => BadRequest(toHateoas(ErrorMessage("Json_parsing_error", s"Json parsing throws an error $errors")))
    }
  }


  def removeRepreSponsor(idPerson: String) = AdminAuthAction {

    ss.removeRepresentative(idPerson)
    Created("Representative has been removed")
  }


  def exportBySponsor(id: Long) = AdminAuthAction {
    val nameSponsor = ss.loadSponsor(id).map(s => StringUtils.stripAccents(s.name.toUpperCase)).getOrElse("NoNameFound")
    val currentDate = LocalDateTime.now()
    val csvFile: File = java.io.File.createTempFile(RandomStringUtils.randomAlphabetic(16), "csv")

    // We force the format for Excel (which is terrible with UTF-8)
    val writer: CSVWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(csvFile), Charset.forName("UTF-8")), ',')
    ss.exportForSponsor(id).foreach(line => {
      writer.writeNext(line.split('|'))
    }
    )
    writer.close()

    val filename = s"$nameSponsor-$currentDate.csv"

    Ok.sendFile(
      content = csvFile,
      inline = false,
      onClose = () => csvFile.delete(),
      fileName = tempFile => filename
    )
  }


  def exportEvent = AdminAuthAction {
    //import better.files._

    val currentDate = LocalDateTime.now()

    val csv: File = java.io.File.createTempFile(System.currentTimeMillis().toString, "")

    //csv.appendLines(ss.exportForEvent: _*)

    val writer = new CSVWriter(new FileWriter(csv), ',', ',')
    ss.exportForEvent.foreach(line => {
      println(line)
      writer.writeNext(line.split('|'))
    }
    )
    writer.close()

    Ok.sendFile(csv).withHeaders((CONTENT_DISPOSITION, s"attachment; filename=allLeads-$currentDate.csv"), (CONTENT_TYPE, "application/x-download"))
  }

  def exportRepresentative(idRepr: String) = AdminAuthAction {

    val currentDate = LocalDateTime.now()

    val csv = java.io.File.createTempFile(System.currentTimeMillis().toString, "")
    /*
    val writer = new CSVWriter(new FileWriter(csv), ',', ',')

    writer.writeNext()*/


    Ok.sendFile(csv).withHeaders((CONTENT_DISPOSITION, s"attachment; filename=$idRepr-$currentDate.csv"), (CONTENT_TYPE, "application/x-download"))
  }

  def checkAdminAuth = AdminAuthAction { implicit request =>
    request.session.get("connected").fold(
      Unauthorized("")
    )(
      mail => Ok(Json.toJson(Map("mail" -> mail)))
    )
  }


  def readAllPersons = AdminAuthAction {
    import model.Person._

    val personJsons: Seq[PersonJson] = ps.allPersons().map(p => Json.parse(p.json).as[PersonJson])


    Ok(Json.toJson(Map("data" -> personJsons.map(pj => Seq(pj.regId, pj.firstname, pj.lastname, pj.email, pj.title, pj.phone.getOrElse("-"), pj.city.getOrElse("-"), pj.company)))))
  }


  def pass = AdminAuthAction { implicit Request =>

    Ok(Json.toJson(Map(ps.pass.map(p => p.regId -> p.pass): _*)))
  }

  def loadAllEvents = AdminAuthAction {


    Ok(Json.toJson(Map("data" -> es.allEvents.map(ev => Seq(ev.typeEvent,ev.message,ev.datetime.toString)))))



  }

  def syncWithMyDevoxx = Action { implicit request =>

    sys.syncMyDevoxx(jsonUtils.tokenExtractorFromSession(request))


    Ok("test")

  }


}

