package controllers

import dao.PersonDAO
import model.ErrorMessage
import play.api.db.Database
import play.api.mvc.{Action, Controller}
import utils.HateoasConverter._
import utils.HateoasUtils.toHateoas

/**
  * Created by fsznajderman on 11/01/2017.
  */
class PersonController(db: Database) extends Controller {

  def readPerson(id: Long) = Action { implicit request =>

    db.withConnection { implicit c =>
      PersonDAO.find(id)
    } match {
      case Some(person) => Ok(toHateoas(person))
      case _ => NotFound(toHateoas(ErrorMessage("Person_not_found", s"Person with id $id not found")))
    }
  }

}