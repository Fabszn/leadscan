package controllers

import java.time.LocalDateTime

import io.github.hamsters.Validation
import model._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}
import play.api.mvc.{Action, Controller}
import services.{LeadService, NotificationService, PersonService}
import utils.HateoasUtils._
import utils.{CORSAction, LoggerAudit}

/**
  * Created by fsznajderman on 24/01/2017.
  */
class LeadController(ls: LeadService, ns: NotificationService, ps: PersonService) extends Controller with LoggerAudit {

  case class LeadFromRequest(idApplicant: Long, idTarget: Long, note: Option[String])

  implicit val leadReader: Reads[LeadFromRequest] = (
    (__ \ "idApplicant").read[Long] and (__ \ "idTarget").read[Long] and (__ \ "note").readNullable[String]
    ) (LeadFromRequest.apply _)

  //save the lead and eventually note
  // for each person


  def lead = CORSAction(parse.json) { implicit request => {


    implicit val jsonListConverter = Reads.seq[LeadFromRequest]

    request.body.validate[Seq[LeadFromRequest]].asEither match {
      case Left(errors) => BadRequest(toHateoas(ErrorMessage("Json_parsing_error", s"Json parsing throws an error ${errors}")))
      case Right(leads) => {
        val tLeads = leads.map(l =>
          (convert2Lead(l), convert2LeadNote(l)))

        val v: Validation[(Lead, Option[LeadNote])] = Validation(tLeads.map(t => ls.isAlreadyConnect(t._1) match {
          case None => Validation.OK(t)
          case Some(_) => Validation.KO(t)
        }): _*)


        val validLead = tLeads.filterNot(item => v.failures.contains(item))

        val persons: Seq[Option[CompletePerson]] = validLead.map(item => {
          ls.addLead(item._1, item._2)
          sendNotification(item)
          ps.getCompletePerson(item._1.idTarget)
        }) ++ v.failures.map(item => {
          item._2.foreach(note => ls.addNote(note))
          ps.getCompletePerson(item._1.idTarget)
        })

        Ok(toHateoas(
          for (
            p <- persons
          ) yield p.get
        ))


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

  def readNote(idNote: Long) = CORSAction { implicit request =>
    ls.getNote(idNote).fold(
      NotFound(toHateoas(ErrorMessage("Note_not_found", s"Note not found in request")))
    )(n => Ok(toHateoas(n)))

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


  private def sendNotification(item: (Lead, Option[LeadNote])) = {
    ns.addNotification(Notification(id = None,
      idRecipient = item._1.idTarget,
      idRequester = item._1.idApplicant,
      NotificationType.Connected.id.toLong,
      NotificationStatus.READ,
      LocalDateTime.now()))
  }

  def testJson = Action(parse.json) { implicit request =>


    val re: JsResult[Seq[LeadFromRequest]] = request.body.validate[Seq[LeadFromRequest]]


    Ok("")

  }

}
