package controllers

import java.time.LocalDateTime

import dao.LeadDAO.Item
import model.ErrorMessage
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Reads, _}
import play.api.mvc.{Action, Controller}
import services.{PersonService, RemoteClient, SponsorService, StatsService}
import utils.HateoasUtils.toHateoas
import utils.LoggerAudit
import utils.oAuthActions.AdminAuthAction

/**
  * Created by fsznajderman on 10/02/2017.
  */
class AdminController(ps: PersonService, ss: SponsorService, sts: StatsService, remote: RemoteClient) extends Controller with LoggerAudit {


  def index = Action {
    Ok(views.html.index())
  }


  def person = AdminAuthAction {
    Ok(views.html.person())
  }

  def stats = AdminAuthAction {
    Ok(views.html.stats())
  }

  def export = AdminAuthAction {

    Ok(views.html.export())
  }

  def statsData = AdminAuthAction {
    val points = sts.getData.leadsDateTime.map(i => JsNumber(Item.tupleFormated(i)._1))
    val dataTime = sts.getData.leadsDateTime.map(i => JsString(Item.tupleFormated(i)._2))
    val nbLead = sts.getData.sponsorStat.map(i => JsNumber(i._1))
    val sponsors = sts.getData.sponsorStat.map(i => JsString(i._2))

    Ok(Json.toJson(Map("points" -> points, "datetime" -> dataTime, "nbLead" -> nbLead, "sponsors" -> sponsors)))
  }


  def all() = AdminAuthAction {

    Ok(Json.toJson(Map("data" -> ps.allPersons().map(p => Seq(p.id.get.toString, p.firstname, p.lastname)))))
  }


  def linkRepreSponsor = AdminAuthAction(parse.json) { implicit request =>

    case class RepresentativeSponsor(idPerson: Long, idSponsor: Long)

    implicit val repSpoReaader: Reads[RepresentativeSponsor] = (
      (__ \ "idPerson").read[Long] and (__ \ "idSponsor").read[Long]
      ) (RepresentativeSponsor.apply _)


    request.body.validate[RepresentativeSponsor].asEither match {
      case Right(link) => {
        ss.addRepresentative(link.idPerson, link.idSponsor)
        Created("Representative and sponsor are associated")
      }
      case Left(errors) => BadRequest(toHateoas(ErrorMessage("Json_parsing_error", s"Json parsing throws an error ${errors}")))
    }

  }


  def newPerson = AdminAuthAction(parse.json) { implicit request =>
    case class NewPerson(firstname: String, lastname: String, email: String, company: String, title: String)

    implicit val newPersonReaader: Reads[NewPerson] = (
      (__ \ "firstname").read[String] and (__ \ "lastname").read[String] and (__ \ "email").read[String] and (__ \ "company").read[String] and (__ \ "title").read[String]
      ) (NewPerson.apply _)


    request.body.validate[NewPerson].asEither match {
      case Right(p) => {
        val pj = ps.addRepresentative(p.firstname, p.lastname, p.email, p.company, p.title)
        //send person to Mydevoxx
        val res = remote.sendPerson(pj, jsonUtils.tokenExtractorFromSession(request))

        Created("representative has been created")
      }
      case Left(errors) => BadRequest(toHateoas(ErrorMessage("Json_parsing_error", s"Json parsing throws an error ${errors}")))
    }
  }


  def removeRepreSponsor(idPerson: Long) = AdminAuthAction {

    ss.removeRepresentative(idPerson)
    Created("Representative has been removed")
  }


  def exportBySponsor(id: Long) = Action {
    import better.files._

    val nameSponsor = ss.loadSponsor(id).map(s => s.name).getOrElse("NoNameFound")
    val currentDate = LocalDateTime.now()

    val csv: File = java.io.File.createTempFile(System.currentTimeMillis().toString, "").getAbsolutePath.toFile

    csv.appendLines(ss.exportForSponsor(id): _*)


    Ok.sendFile(csv.toJava).withHeaders((CONTENT_DISPOSITION, s"attachment; filename=$nameSponsor-$currentDate.csv"), (CONTENT_TYPE, "application/x-download"))
  }


  def exportEvent = Action {
    import better.files._

    val currentDate = LocalDateTime.now()

    val csv: File = java.io.File.createTempFile(System.currentTimeMillis().toString, "").getAbsolutePath.toFile

    csv.appendLines(ss.exportForEvent: _*)


    Ok.sendFile(csv.toJava).withHeaders((CONTENT_DISPOSITION, s"attachment; filename=allLeads-$currentDate.csv"), (CONTENT_TYPE, "application/x-download"))
  }

  def checkAuth = AdminAuthAction { implicit request =>
    request.session.get("connected").fold(
      Unauthorized("")
    )(
      mail => Ok(Json.toJson(Map("mail" -> mail)))
    )
  }

}
