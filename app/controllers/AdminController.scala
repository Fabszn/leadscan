package controllers

import java.time.LocalDateTime

import dao.LeadDAO.Item
import model.{ErrorMessage, PersonJson}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Reads, _}
import play.api.mvc.{Action, Controller}
import services._
import utils.HateoasUtils.toHateoas
import utils.oAuthActions.{AdminAuthAction, AdminRootAction, ReportsAuthAction}
import utils.{LoggerAudit, PasswordGenerator}

/**
  * Created by fsznajderman on 10/02/2017.
  */
class AdminController(ps: PersonService, ss: SponsorService, sts: StatsService, ns: NotificationService, remote: RemoteClient) extends Controller with LoggerAudit {


  def index = AdminRootAction {
    Ok(views.html.reports())
  }

  def admin = AdminRootAction {
    Ok(views.html.admin())
  }

  def person = AdminAuthAction {
    Ok(views.html.person())
  }

  def stats = AdminAuthAction {
    Ok(views.html.stats())
  }

  def statsBySponsor = ReportsAuthAction {
    Ok(views.html.statsBySponsor())
  }

  def export = AdminAuthAction {

    Ok(views.html.export())
  }

  def repreSpnsor = AdminAuthAction {

    Ok(views.html.onlyReprentative())
  }

  def passView = AdminAuthAction {
    Ok(views.html.pass())
  }

  def statsData = AdminAuthAction {
    val points = sts.getData.leadsDateTime.map(i => JsNumber(Item.tupleFormated(i)._1))
    val dataTime = sts.getData.leadsDateTime.map(i => JsString(Item.tupleFormated(i)._2))
    val nbLead = sts.getData.sponsorStat.map(i => JsNumber(i._1))
    val sponsors = sts.getData.sponsorStat.map(i => JsString(i._2))

    Ok(Json.toJson(Map("points" -> points, "datetime" -> dataTime, "nbLead" -> nbLead, "sponsors" -> sponsors)))
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

        Ok(Json.toJson(Map("points" -> points, "datetime" -> dataTime, "nbLead" -> nbLead, "sponsors" -> sponsors, "pointsGlobal" -> pointsGlobal,"dataTimeGlobable" -> dataTimeGlobable )))
      }
      case None => Unauthorized("You are not representative of one sponsor")

    }
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
        val token = jsonUtils.tokenExtractorFromSession(request)

        for {
          p <- ps.getCompletePerson(link.idPerson)
          s <- ss.loadSponsor(link.idSponsor)
        } yield {
          import scala.concurrent.ExecutionContext.Implicits.global
          val pass = PasswordGenerator.generatePassword
          ps.addpass(p.regId, pass)
          remote.sendPassword(p.regId, pass, token).foreach { _ =>
            ns.sendMail(
              Seq(p.email),
              Option(views.txt.mails.notifPassword.render(p.firstname, s.name, pass, p.email).body),
              Option(views.html.mails.notifPassword.render(p.firstname, s.name, pass, p.email).body)
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
        val token = jsonUtils.tokenExtractorFromSession(request)
        val pj = ps.addRepresentative(p.firstname, p.lastname, p.email.toLowerCase(), p.company, p.title, token)
        //send person to Mydevoxx
        remote.sendPerson(pj, token)

        Created("representative has been created")
      }
      case Left(errors) => BadRequest(toHateoas(ErrorMessage("Json_parsing_error", s"Json parsing throws an error $errors")))
    }
  }


  def removeRepreSponsor(idPerson: String) = AdminAuthAction {

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

  def exportRepresentative(idRepr: String) = Action {
    import better.files._

    val currentDate = LocalDateTime.now()

    val csv: File = java.io.File.createTempFile(System.currentTimeMillis().toString, "").getAbsolutePath.toFile

    csv.appendLines(ss.exportForRepresentative(idRepr): _*)


    Ok.sendFile(csv.toJava).withHeaders((CONTENT_DISPOSITION, s"attachment; filename=$idRepr-$currentDate.csv"), (CONTENT_TYPE, "application/x-download"))
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


    Ok(Json.toJson(Map("data" -> personJsons.map(pj => Seq(pj.regId, pj.firstname, pj.lastname, pj.email, pj.title.getOrElse("-"), pj.phone.getOrElse("-"), pj.city.getOrElse("-"), pj.company.getOrElse("-"))))))
  }


  def pass = AdminAuthAction { implicit Request =>

    Ok(Json.toJson(Map(ps.pass.map(p => p.regId -> p.pass): _*)))
  }

  def test = Action { implicit request =>

    println(jsonUtils.tokenExtractorFromSession(request))


    Ok("test")

  }


}
