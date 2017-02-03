package controllers

import java.time.LocalDateTime

import model._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}
import play.api.mvc.{Action, Controller}
import services.{LeadService, NotificationService}
import utils.HateoasUtils._
import utils.LoggerAudit

/**
  * Created by fsznajderman on 24/01/2017.
  */
class LeadController(ls: LeadService, ns: NotificationService) extends Controller with LoggerAudit {


  def lead = Action(parse.json) { implicit request => {

    implicit val leadReader: Reads[Lead] = (
      (__ \ "idApplicant").read[Long] and (__ \ "idTarget").read[Long]
      ) (Lead.apply _)


    val miseEnContact = request.body.validate[Lead].get

    ls.isAlreadyConnect(miseEnContact) match {
      case Some(_) => Conflict(toHateoas(InfoMessage(s"Connection betwwen person with id ${miseEnContact.idApplicant} and person with id ${miseEnContact.idTarget} is already exists")))
      case None =>
        ls.addLead(miseEnContact)
        ns.addNotification(Notification(id = None,
          idRecipient = miseEnContact.idTarget,
          idRequester = miseEnContact.idApplicant,
          NotificationType.Connected.id.toLong,
          NotificationStatus.READ,
          LocalDateTime.now()))
        Created(toHateoas(InfoMessage(s"person with id ${miseEnContact.idApplicant} has been connected with person with id ${miseEnContact.idTarget}")))
    }
  }
  }


  def leads(id: Long) = Action { implicit request =>

    ls.getLeads(id) match {
      case Nil => NotFound(toHateoas(ErrorMessage("leads_not_found", s"Leads for person with id ${id} are not found")))
      case leads => Ok(toHateoas(leads))
    }


  }

}
