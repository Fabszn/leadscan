package controllers

import java.time.{LocalDateTime, ZoneOffset}

import io.github.hamsters.Validation
import model._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}
import play.api.mvc.Controller
import services.{LeadService, NotificationService, PersonService}
import utils.HateoasUtils._
import utils.oAuthActions.ApiAuthAction
import utils.{CORSAction, LoggerAudit}

/**
  * Created by fsznajderman on 24/01/2017.
  */
class LeadController(ls: LeadService, ns: NotificationService, ps: PersonService) extends Controller with LoggerAudit {

  case class TargetInfo(idTarget: String, note: Option[String])

  case class LeadFromRequest(idApplicant: String, idTarget: String, note: Option[String])

  implicit val targetsreads: Reads[TargetInfo] = (
    (__ \ "id").read[String] and (__ \ "note").readNullable[String]
    ) (TargetInfo.apply _)


  def lead = CORSAction(ApiAuthAction(parse.json) {
    implicit request => {




      val json = request.body
      //specific request.body parser.
      (json \ "idApplicant").validate[String].asEither match {
        case Left(erIdApplicant) => BadRequest(toHateoas(ErrorMessage("Json_parsing_error", s"Json parsing throws an error ${erIdApplicant}")))
        case Right(idApplicant) => {

          (json \ "targets").validate[Seq[TargetInfo]].asEither match {
            case Left(eTarget) => BadRequest(toHateoas(ErrorMessage("Json_parsing_error", s"Json parsing throws an error ${eTarget}")))
            case Right(targets) => {

              val filteredTarget: (Seq[TargetInfo], Seq[TargetInfo]) = targets.partition(target => ls.isExists(target.idTarget).isDefined)

              val tLeads = filteredTarget._1.map(item => LeadFromRequest(idApplicant, item.idTarget, item.note)).map(l =>
                (convert2Lead(l), convert2LeadNote(l)))

              val v: Validation[(Lead, Option[LeadNote])] = Validation(tLeads.map(t => ls.isAlreadyConnect(t._1) match {
                case None => Validation.OK(t)
                case Some(_) => Validation.KO(t)
              }): _*)


              val validLead = tLeads.filterNot(item => v.failures.contains(item))

              val idTargets = validLead.map(item => {
                ls.addLead(item._1, item._2)
                sendNotification(item)
                item._1.idTarget
              }) ++ v.failures.map(item => {
                item._2.foreach(note => ls.addNote(note))
                item._1.idTarget
              })


              Ok(toHateoas(
                for (
                  p <- ls.getCompleteLeads(idApplicant).filter(cpwn => idTargets.contains(cpwn.person.regId))
                ) yield p
              ) ++ Json.obj("targetsNotFound" -> filteredTarget._2.map(t => t.idTarget.toString)))
            }
          }
        }
      }
    }

  })


  def addNote = CORSAction {
    ApiAuthAction(parse.json) {
      implicit request =>

        implicit val leadReader: Reads[LeadFromRequest] = (
          (__ \ "idApplicant").read[String] and (__ \ "idTarget").read[String] and (__ \ "note").readNullable[String]
          ) (LeadFromRequest.apply _)

        request.body.validate[LeadFromRequest].asEither match {
          case Left(errors) => BadRequest(toHateoas(ErrorMessage("Json_parsing_error", s"Json parsing throws an error ${
            errors
          }")))
          case Right(leadFromRequest) => {

            val lead = convert2Lead(leadFromRequest)
            val leadNote: Option[LeadNote] = convert2LeadNote(leadFromRequest)
            ls.isAlreadyConnect(lead).fold {
              BadRequest(toHateoas(ErrorMessage("person_not_connected"
                , s"Person with id ${
                  lead.idApplicant
                } is not already connected with person with id ${
                  lead.idTarget
                }")))
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
  }

  def readNotes(idAppliquant: String) = CORSAction {
    ApiAuthAction {
      implicit request =>
        Ok(toHateoas(ls.getNotes(idAppliquant)))
    }
  }

  def readNote(idNote: Long) = CORSAction {
    ApiAuthAction {
      implicit request =>
        ls.getNote(idNote).fold(
          NotFound(toHateoas(ErrorMessage("Note_not_found", s"Note not found in request")))
        )(n => Ok(toHateoas(n)))

    }
  }


  def leads(id: String) = CORSAction {
    ApiAuthAction {
      implicit request => {

        val regId = jsonUtils.extractRegIdFromToken(request)
        logger.info(s"regId found $regId")
        ls.getCompleteLeads(regId) match {
          case Nil => NotFound(toHateoas(ErrorMessage("leads_not_found", s"Leads for person with id ${regId} are not found")))
          case leads => Ok(toHateoas(leads))
        }
      }
    }


  }

  def latestLeads(datetime:Long) = CORSAction {
    ApiAuthAction {
      implicit request => {




        val regId = jsonUtils.extractRegIdFromToken(request)
        logger.info(s"regId found $regId")
        ls.getCompleteLatestLeads(regId,LocalDateTime.ofEpochSecond(datetime, 0, ZoneOffset.UTC)) match {
          case Nil => NotFound(toHateoas(ErrorMessage("leads_not_found", s"Leads for person with id ${regId} are not found")))
          case leads => Ok(toHateoas(leads))
        }
      }
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


}
