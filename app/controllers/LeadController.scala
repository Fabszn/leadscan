package controllers

import model.{InfoMessage, Lead}
import play.api.libs.json.{Reads, _}
import play.api.mvc.{Action, Controller}
import services.LeadService
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import utils.HateoasUtils.toHateoas

/**
  * Created by fsznajderman on 24/01/2017.
  */
class LeadController(ls: LeadService) extends Controller {


  def connect = Action(parse.json) { implicit request => {


    implicit val leadReader: Reads[Lead] = (
      (__ \ "idApplicant").read[Long] and (__ \ "idTarget").read[Long]
      ) (Lead.apply _)


    val miseEnContact = request.body.validate[Lead].get

    ls.isAlreadyConnect(miseEnContact) match {
      case Some(c) => Conflict(toHateoas(InfoMessage(s"Connection betwwen person with id ${miseEnContact.idApplicant} and person with id ${miseEnContact.idTarget} is already exists")))
      case None =>
        ls.addLead(miseEnContact)
        Created(toHateoas(InfoMessage(s"person with id ${miseEnContact.idApplicant}1 has been connected with person with id ${miseEnContact.idTarget}")))


    }


  }

  }

}
