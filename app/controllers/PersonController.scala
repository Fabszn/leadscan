package controllers

import model._
import play.api.mvc.Controller
import services.{PersonService, UpdatePerson}
import utils.HateoasUtils.toHateoas
import utils.oAuthActions.ApiAuthAction
import utils.{CORSAction, LoggerAudit}

/**
  * Created by fsznajderman on 11/01/2017.
  */
class PersonController(ps: PersonService) extends Controller with LoggerAudit {

  def read(id: String) = CORSAction {
    ApiAuthAction(parse.json) { implicit request => {
      ps.getPerson(id) match {
        case Some(person) => Ok(toHateoas(person))
        case _ => NotFound(toHateoas(ErrorMessage("Person_not_found", s"Person with id $id not found")))
      }
    }
    }
  }



  def maj(id: String) = CORSAction {
    ApiAuthAction(parse.json) { implicit request =>

      import jsonUtils._

      val j = request.body


      val strFields = jsonToMapExtractor[String](List("firstname", "lastname"), j)
      val intFields = jsonToMapExtractor[Int](List("age", "experience"), j)
      val boolFields = jsonToMapExtractor[Boolean](List("isTraining"), j)

      ps.majPerson(id, UpdatePerson(strFields, intFields, boolFields))


      Accepted(toHateoas(InfoMessage(s"Person with id $id updated")))

    }


  }
}
