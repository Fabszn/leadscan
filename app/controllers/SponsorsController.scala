package controllers

import model.{ErrorMessage, InfoMessage, Sponsor}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Json, Reads, _}
import play.api.mvc.Controller
import services.SponsorService
import utils.HateoasUtils._
import utils.oAuthActions.AdminAuthAction

/**
  * Created by fsznajderman on 11/02/2017.
  */
class SponsorsController(ss: SponsorService) extends Controller {

  implicit val readSponsor: Reads[Sponsor] = (
    (__ \ "id").readNullable[Long] and (__ \ "name").read[String]
      and (__ \ "level").read[String]
    ) (Sponsor.apply _)

  def view = AdminAuthAction {
    Ok(views.html.admin.sponsors())
  }


  def read(id: String) = AdminAuthAction { implicit request =>

    implicit val sponsor2json: Writes[Sponsor] = (

      (JsPath \ "id").write[Option[Long]] and
        (JsPath \ "name").write[String] and
        (JsPath \ "level").write[String]
      ) (unlift(Sponsor.unapply))

    ss.loadSponsor(id.toInt).fold(NotFound(toHateoas(ErrorMessage("Sponsor_not_found", s"sponsor with id $id hasn't been found"))))(sponsor =>
      Ok(Json.toJson(sponsor))
    )

  }

  def modify = AdminAuthAction(parse.json) { implicit request =>

    request.body.validate[Sponsor].asEither match {
      case Right(s) =>
        ss.modifySponsor(s)
        Created(toHateoas(InfoMessage("Sponsor has been updated")))
      case Left(e) => InternalServerError(e.toString())
    }

  }

  def readAll = AdminAuthAction {

    Ok(Json.toJson(Map("data" -> ss.loadSponsors().map(s => Seq(s.id.get.toString, s.name, s.level)))))
  }


  def add() = AdminAuthAction(parse.json) { implicit request =>


    request.body.validate[Sponsor].asEither match {
      case Right(s) => {
        ss.addSponsor(s)
        Created(toHateoas(InfoMessage("Sponsor has been created")))
      }
      case Left(e) => InternalServerError(e.toString())
    }


  }

  def readAllRepr = AdminAuthAction { implicit Request =>

    Ok(Json.toJson(Map("data" -> ss.loadRepresentative().map(p => Seq(p.idPerson.toString, p.firstname, p.lastname, p.idSponsor.map(_.toString).getOrElse("-"), p.nameSponsor.getOrElse("-"))))))

  }

  def readOnlyReprBySponsor(idSponsor:Long) = AdminAuthAction { implicit Request =>
    Ok(Json.toJson(Map("data" -> ss.loadOnlyRepresentative(idSponsor).map(p => Seq(p.idPerson.toString, s"${p.firstname} ${p.lastname}",s"${p.email}",  p.nameSponsor.getOrElse("-"),p.idSponsor.map(_.toString).getOrElse("-"),"")))))
  }

  def readOnlyRepr = AdminAuthAction { implicit Request =>
    Ok(Json.toJson(Map("data" -> ss.loadOnlyRepresentative.map(p => Seq(p.idPerson.toString, s"${p.firstname} ${p.lastname}",s"${p.email}",  p.nameSponsor.getOrElse("-"),p.idSponsor.map(_.toString).getOrElse("-"),"")))))
  }


}
