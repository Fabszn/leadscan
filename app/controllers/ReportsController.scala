package controllers

import controllers.jsonUtils.{regIdExtractorReports, tokenExtractorFromSession}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.SponsorService
import utils.LoggerAudit
import utils.oAuthActions.ReportsAuthAction

/**
  * Created by fsznajderman on 27/03/2017.
  */
class ReportsController(ss: SponsorService) extends Controller with LoggerAudit {

  def reports = Action {

    Ok(views.html.reports())

  }

  def representatives = Action {

    Ok(views.html.reprentativeBySponsor())

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

}
