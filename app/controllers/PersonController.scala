package controllers

import model.{ErrorMessage, InfoMessage}
import play.api.mvc.{Action, Controller}
import services.{PersonService, UpdatePerson}
import utils.HateoasConverter._
import utils.HateoasUtils.toHateoas
import utils.LoggerAudit

/**
  * Created by fsznajderman on 11/01/2017.
  */
class PersonController(ps: PersonService) extends Controller with LoggerAudit {

  def read(id: Long) = Action { implicit request =>
    ps.getPerson(id) match {
      case Some(person) => Ok(toHateoas(person)).withHeaders(("Access-Control-Allow-Origin","*"))
      case _ => NotFound(toHateoas(ErrorMessage("Person_not_found", s"Person with id $id not found"))).withHeaders(("Access-Control-Allow-Origin","*"))
    }
  }

  def readSensitive(id: Long) = Action { implicit request =>
    ps.getPersonSensitive(id) match {
      case Some(pSensitive) => Ok(toHateoas(pSensitive)).withHeaders(("Access-Control-Allow-Origin","*"))
      case _ => NotFound(toHateoas(ErrorMessage("Person_sensitive_not_found", s"Person sensitive with id $id not found"))).withHeaders(("Access-Control-Allow-Origin","*"))
    }
  }

  def maj(id: Long) = Action(parse.json) { implicit request =>

    import jsonUtils._

    val j = request.body


    val strFields = jsonToMapExtractor[String](List("firstname", "lastname"), j)
    val intFields = jsonToMapExtractor[Int](List("age", "experience"), j)
    val boolFields = jsonToMapExtractor[Boolean](List("isTraining"), j)

    ps.majPerson(id, UpdatePerson(strFields, intFields, boolFields))


    Accepted(toHateoas(InfoMessage(s"Person with id $id updated")))

  }
}
