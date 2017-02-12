package controllers

import java.time.LocalDateTime

import model._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}
import play.api.mvc.Controller
import services.{LeadService, NotificationService}
import utils.HateoasUtils._
import utils.{CORSAction, LoggerAudit}

/**
  * Created by fsznajderman on 24/01/2017.
  */
class LeadController(ls: LeadService, ns: NotificationService) extends Controller with LoggerAudit {

  case class LeadFromRequest(idApplicant: Long, idTarget: Long, note: Option[String])

  implicit val leadReader: Reads[LeadFromRequest] = (
    (__ \ "idApplicant").read[Long] and (__ \ "idTarget").read[Long] and (__ \ "note").readNullable[String]
    ) (LeadFromRequest.apply _)

  def lead = CORSAction(parse.json) { implicit request => {

    request.body.validate[LeadFromRequest].asEither match {
      case Left(errors) => BadRequest(toHateoas(ErrorMessage("Json_parsing_error", s"Json parsing throws an error ${errors}")))
      case Right(miseEnContact) => {
        val lead = convert2Lead(miseEnContact)
        val leadNote = convert2LeadNote(miseEnContact)

        ls.isAlreadyConnect(lead) match {
          case Some(_) => Conflict(toHateoas(InfoMessage(s"Connection between person with id ${miseEnContact.idApplicant} and person with id ${miseEnContact.idTarget} is already exists")))
          case None =>
            ls.addLead(lead, leadNote)
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
  }
  }

  def addNote = CORSAction(parse.json) { implicit request =>

    request.body.validate[LeadFromRequest].asEither match {
      case Left(errors) => BadRequest(toHateoas(ErrorMessage("Json_parsing_error", s"Json parsing throws an error ${errors}")))
      case Right(leadFromRequest) => {

        val lead = convert2Lead(leadFromRequest)
        val leadNote: Option[LeadNote] = convert2LeadNote(leadFromRequest)
        ls.isAlreadyConnect(lead).fold {
          BadRequest(toHateoas(ErrorMessage("person_not_connected"
            , s"Person with id ${lead.idApplicant} is not already connected with person with id ${lead.idTarget}")))
        } {
          _ =>
            leadNote match {
              case Some(ln) => {
                ls.addNote(ln)
                Created(toHateoas(InfoMessage("Note added successfully")))
              }
              case None => BadRequest(toHateoas(ErrorMessage("Note_not_found", s"Note not found in request")))
            }
        }

      }
    }

  }

  def readNotes(idAppliquant: Long) = CORSAction { implicit request =>
    Ok(toHateoas(ls.getNotes(idAppliquant)))
  }


  def leads(id: Long) = CORSAction { implicit request =>

    ls.getLeads(id) match {
      case Nil => NotFound(toHateoas(ErrorMessage("leads_not_found", s"Leads for person with id ${id} are not found")))
      case leads => Ok(toHateoas(leads))
    }


  }

  private def convert2Lead(leadFromRequest: LeadFromRequest): Lead = {
    Lead(leadFromRequest.idApplicant, leadFromRequest.idTarget)
  }

  private def convert2LeadNote(leadFromRequest: LeadFromRequest): Option[LeadNote] = {
    leadFromRequest.note.map(n => LeadNote(None, leadFromRequest.idApplicant, leadFromRequest.idTarget, n))
  }

}
