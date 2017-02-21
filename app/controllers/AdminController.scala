package controllers

import java.time.LocalDateTime

import model.ErrorMessage
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Json, Reads, _}
import play.api.mvc.{Action, Controller}
import services.{PersonService, SponsorService}
import utils.HateoasUtils.toHateoas
import utils.LoggerAudit

/**
  * Created by fsznajderman on 10/02/2017.
  */
class AdminController(ps: PersonService, ss: SponsorService) extends Controller with LoggerAudit {


  def index = Action {
    Ok(views.html.index())
  }

  def home = Action {
    Ok(views.html.home())
  }

  def person = Action {
    Ok(views.html.person())
  }

  def stats = Action {
    Ok(views.html.stats())
  }


  def all() = Action {

    Ok(Json.toJson(Map("data" -> ps.allPersons().map(p => Seq(p.id.get.toString, p.firstname, p.lastname)))))
  }


  def linkRepreSponsor = Action(parse.json) { implicit request =>

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


  def newPerson = Action(parse.json) { implicit request =>
    case class NewPerson(firstname: String, lastname: String, email: String)

    implicit val newPersonReaader: Reads[NewPerson] = (
      (__ \ "firstname").read[String] and (__ \ "lastname").read[String] and (__ \ "email").read[String]
      ) (NewPerson.apply _)


    request.body.validate[NewPerson].asEither match {
      case Right(p) => {
        ps.addRepresentative(p.firstname, p.lastname, p.email)
        Created("representative has been created")
      }
      case Left(errors) => BadRequest(toHateoas(ErrorMessage("Json_parsing_error", s"Json parsing throws an error ${errors}")))
    }
  }


  def removeRepreSponsor(idPerson: Long) = Action {

    ss.removeRepresentative(idPerson)
    Created("Representative has been removed")
  }


  def displayExport = Action {

    Ok(views.html.export())
  }


  def exportBySponsor(id: Long) = Action {
    import better.files._

    val nameSponsor = ss.loadSponsor(id).map(s => s.name).getOrElse("NoNameFound")
    val currentDate = LocalDateTime.now()

    val csv: File = java.io.File.createTempFile(System.currentTimeMillis().toString, "").getAbsolutePath.toFile

    csv.appendLines(ss.export(id): _*)


    Ok.sendFile(csv.toJava).withHeaders((CONTENT_DISPOSITION, s"attachment; filename=$nameSponsor-$currentDate.csv"), (CONTENT_TYPE, "application/x-download"))
  }

}
