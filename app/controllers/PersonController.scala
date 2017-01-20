package controllers

import model.ErrorMessage
import play.api.mvc.{Action, Controller}
import services.PersonService
import utils.HateoasConverter._
import utils.HateoasUtils.toHateoas

/**
  * Created by fsznajderman on 11/01/2017.
  */
class PersonController(ps: PersonService) extends Controller {

  def readPerson(id: Long) = Action { implicit request =>
    ps.getPerson(id) match {
      case Some(person) => Ok(toHateoas(person))
      case _ => NotFound(toHateoas(ErrorMessage("Person_not_found", s"Person with id $id not found")))
    }
  }
}
