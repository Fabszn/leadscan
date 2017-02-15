package controllers

import dao.SponsorDAO.PersonSponsorInfo
import model.{ErrorMessage, InfoMessage, Sponsor}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Json, Reads, _}
import play.api.mvc.{Action, Controller}
import services.SponsorService
import utils.HateoasUtils._

/**
  * Created by fsznajderman on 11/02/2017.
  */
class SponsorsController(ss: SponsorService) extends Controller {

  implicit val readSponsor: Reads[Sponsor] = (
    (__ \ "id").readNullable[Long] and (__ \ "name").read[String]
      and (__ \ "level").read[String]
    ) (Sponsor.apply _)

  def view = Action {
    Ok(views.html.sponsors())
  }


  def read(id: Long) = Action { implicit request =>

    implicit val sponsor2json: Writes[Sponsor] = (

      (JsPath \ "id").write[Option[Long]] and
        (JsPath \ "name").write[String] and
        (JsPath \ "level").write[String]
      ) (unlift(Sponsor.unapply))

    ss.loadSponsor(id).fold(NotFound(toHateoas(ErrorMessage("Sponsor_not_found", s"sponsor with id $id hasn't been found"))))(sponsor =>
      Ok(Json.toJson(sponsor))
    )

  }

  def modify = Action(parse.json) { implicit request =>

    request.body.validate[Sponsor].asEither match {
      case Right(s) => {
        ss.modifySponsor(s)
        Created(toHateoas(InfoMessage("Sponsor has been updated")))
      }
      case Left(e) => InternalServerError(e.toString())
    }

  }

  def readAll = Action {

    Ok(Json.toJson(Map("data" -> ss.loadSponsors().map(s => Seq(s.id.get.toString, s.name, s.level)))))
  }


  def add() = Action(parse.json) { implicit request =>


    request.body.validate[Sponsor].asEither match {
      case Right(s) => {
        ss.addSponsor(s)
        Created(toHateoas(InfoMessage("Sponsor has been created")))
      }
      case Left(e) => InternalServerError(e.toString())
    }


  }

  def readAllRepr = Action { implicit Request =>

    /*implicit val personSponsorInfo: Writes[PersonSponsorInfo] = {
      ((__ \ "id").write[Long]
        and (__ \ "firstname").write[String]
        and (__ \ "lastname").write[String]
        and (__ \ "idsponsor").write[Option[Long]]
        and (__ \ "name").write[Option[String]]) (unlift(PersonSponsorInfo.unapply _))
    }*/


    Ok(Json.toJson(Map("data" -> ss.LoadRepresentative().map(p => Seq(p.idPerson.toString, p.firstname, p.lastname, p.idSponsor.map(_.toString).getOrElse("-"), p.nameSponsor.getOrElse("-"))))))

  }


}
