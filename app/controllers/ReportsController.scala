package controllers

import java.io
import java.io.{File, FileOutputStream, OutputStreamWriter}
import java.nio.charset.Charset
import java.time.LocalDateTime

import com.opencsv.CSVWriter
import controllers.jsonUtils.{regIdExtractorReports, tokenExtractorFromSession}
import model.{ErrorMessage, Event, ImportRepresentative}
import org.apache.commons.lang3.{RandomStringUtils, StringUtils}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import repository.LeadDAO.Item
import services._
import utils.HateoasUtils.toHateoas
import utils.oAuthActions.ReportsAuthAction
import utils.{LoggerAudit, PasswordGenerator}

import scala.util.Success


/**
  * Created by fsznajderman on 27/03/2017.
  */
class ReportsController(ss: SponsorService, ps: PersonService, remote: RemoteClient, eventService: EventService, ns: NotificationService, sts: StatsService, ls: LeadService) extends Controller with LoggerAudit {

  def reports = Action {

    Ok(views.html.reports.reports())

  }

  def representatives = Action {
    Ok(views.html.reports.reprentativeBySponsor())
  }

  def leads = Action {
    Ok(views.html.reports.leads())
  }

  def statsBySponsor = ReportsAuthAction {
    Ok(views.html.reports.statsBySponsor())
  }

  def representativesBySponsor = ReportsAuthAction { implicit request =>

    ss.loadSponsorFromRepresentative(regIdExtractorReports(tokenExtractorFromSession(request))) match {
      case Some(sponsor) =>
        Ok(Json.toJson(Map("data" -> ss.loadOnlyRepresentative(sponsor.id.get).map(p => Seq(p.idPerson.toString, s"${p.firstname} ${p.lastname}", s"${p.email}", p.nameSponsor.getOrElse("-"), p.idSponsor.map(_.toString).getOrElse("-"), "")))))
      case None => BadRequest("No sponsor has been found for this profile")

    }
  }

  def checkReportsAuth = ReportsAuthAction { implicit request =>

    logger.info("Report Check auth")
    request.session.get("connected").fold(
      Unauthorized("")
    )(
      mail => Ok(Json.toJson(Map("mail" -> mail)))
    )
  }

  def statsBySponsorData = ReportsAuthAction { implicit request =>
    import jsonUtils._


    ss.loadSponsorFromRepresentative(regIdExtractorReports(tokenExtractorFromSession(request))) match {

      case Some(sponsor) => {
        val points = sts.getDataBySponsor(sponsor.id.get).leadsDateTime.map(i => JsNumber(Item.tupleFormated(i)._1))
        val dataTime = sts.getDataBySponsor(sponsor.id.get).leadsDateTime.map(i => JsString(Item.tupleFormated(i)._2))
        val nbLead = sts.getDataBySponsor(sponsor.id.get).sponsorStat.map(i => JsNumber(i._1))
        val sponsors = sts.getDataBySponsor(sponsor.id.get).sponsorStat.map(i => JsString(i._2))
        val pointsGlobal = sts.getData.leadsDateTime.map(i => JsNumber(Item.tupleFormated(i)._1))
        val dataTimeGlobable = sts.getData.leadsDateTime.map(i => JsString(Item.tupleFormated(i)._2))

        Ok(Json.toJson(Map("points" -> points, "datetime" -> dataTime, "nbLead" -> nbLead, "sponsors" -> sponsors, "pointsGlobal" -> pointsGlobal, "dataTimeGlobable" -> dataTimeGlobable)))
      }
      case None => Unauthorized("You are not representative of one sponsor")

    }
  }


  def newPerson = ReportsAuthAction(parse.json) { implicit request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    case class NewPerson(firstname: String, lastname: String, email: String, company: String, title: String)

    implicit val newPersonReaader: Reads[NewPerson] = (
      (__ \ "firstname").read[String] and (__ \ "lastname").read[String] and (__ \ "email").read[String] and (__ \ "company").read[String] and (__ \ "title").read[String]
      ) (NewPerson.apply _)


    request.body.validate[NewPerson].asEither match {
      case Right(p) => {
        val regId = jsonUtils.regIdExtractorReports(jsonUtils.tokenExtractorFromSession(request))
        ss.loadSponsorFromRepresentative(regId) match {
          case None => BadRequest(toHateoas(ErrorMessage("Sponsor not found", s"Sponsor not found for regid $regId")))
          case Some(s) => {
            val pj = ps.addRepresentative(p.firstname, p.lastname, p.email.toLowerCase().trim, p.company, p.title)
            ss.addRepresentative(pj.regId, s.id.get)
            val pass = PasswordGenerator.generatePassword
            //send person to Mydevoxx
            remote.sendPerson(pj).andThen {
              case Success(_) => {
                ps.addpass(pj.regId, pass)
                remote.sendPassword(pj.regId, pass)
              }
              case _ => eventService.addEvent(Event(typeEvent = ImportRepresentative.typeEvent, message = s"ERROR when create representative / Reports $pj"))
            } andThen {
              case Success(_) => {
                ns.sendMail(
                  Seq(p.email),
                  Option(views.txt.mails.notifPassword.render(p.firstname, s.name, pass, p.email, "").body),
                  Option(views.html.mails.notifPassword.render(p.firstname, s.name, pass, p.email, "").body)
                )
              }
              case _ => eventService.addEvent(Event(typeEvent = ImportRepresentative.typeEvent, message = s"ERROR when send email representative / Reports $pj"))
            }

            Created("representative has been created")
          }

        }


      }
      case Left(errors)
      => BadRequest(toHateoas(ErrorMessage("Json_parsing_error", s"Json parsing throws an error $errors")))
    }
  }

  def leadsBySponsor = ReportsAuthAction { implicit request =>


    val regId = regIdExtractorReports(tokenExtractorFromSession(request))
    ss.loadSponsorFromRepresentative(regId) match {
      case None => NotFound(s"None sponsor reference found for this regID : ${regId}")
      case Some(sponsor) => {
        val personJsons = ss.loadScannedPersonBySponsor(sponsor.id.get)

        Ok(Json.toJson(Map("data" -> personJsons.map(pj => Seq(pj.regId, pj.firstname, pj.lastname, pj.email, pj.title, pj.phone.getOrElse("-"), pj.city.getOrElse("-"), pj.company)))))
      }
    }

  }

  def export = ReportsAuthAction { implicit request =>
    val regId = regIdExtractorReports(tokenExtractorFromSession(request))

    ss.loadSponsorFromRepresentative(regId) match {
      case Some(s) => {
        val currentDate = LocalDateTime.now()
        val csvFile: File = java.io.File.createTempFile(RandomStringUtils.randomAlphabetic(16), "csv")

        // We force the format for Excel (which is terrible with UTF-8)
        val writer: CSVWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(csvFile), Charset.forName("UTF-8")), ',')
        ss.exportForSponsor(s.id.get).foreach(line => {
          writer.writeNext(line.split('|'))
        }
        )
        writer.close()

        val filename = s"${s.name.toUpperCase}-$currentDate.csv"

        Ok.sendFile(
          content = csvFile,
          inline = false,
          onClose = () => csvFile.delete(),
          fileName = tempFile => filename
        )
      }
      case None => BadRequest("No sponsor found")
  }
  }


}
