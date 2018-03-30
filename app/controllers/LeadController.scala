package controllers

import java.time.{LocalDateTime, ZoneOffset}

import io.github.hamsters.Validation
import model._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}
import play.api.mvc.{Controller, Result, Results}
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


  def readLead(slug: String, idAttendee: String) = {
    CORSAction(ApiAuthAction {
      implicit request => {

        ls.isAlreadyConnect(Lead(slug, idAttendee)).fold(NotFound(s"None lead found for ${slug} / ${idAttendee}"))(l
        => {

          val lobject = Json.obj("lead" -> l)

          Ok(Json.toJson(ls.getNote(l.idApplicant, l.idTarget).fold(lobject)(n => lobject ++ Json.obj("message" ->
            Json.toJson(n.note)))))


        })

      }
    })

  }

  def deleteLead = {
    CORSAction(ApiAuthAction(parse.json) {
      implicit request => {

        val leadIds = request.body.as[LeadIDs]

        ls.deleteLead(leadIds)


        Ok(s"lead for ${leadIds.slug} / ${leadIds.idAttendee} has been deleted")

      }
    })

  }

  def lead = {
    CORSAction(ApiAuthAction(parse.json) {
      implicit request => {

        request.body.validate[LeadGluon] match {
          case JsError(l) => BadRequest(s"Payload has bad format. Details =>  ${l}")
          case JsSuccess(r, _) => manageNewLead(r)
        }
      }

    })
  }

  private def manageNewLead(l: LeadGluon): Result = {


    val pj = PersonJson(l.idAttendee, None, l.firstName, l.lastName, l.email, "-", l.company)
    val leadNote = buildNote(l)

    ls.isAlreadyConnect(Lead(l.slug, l.idAttendee)) match {
      case Some(_) => {

        //update note
        leadNote.fold()(l => ls.addNote(l))

        Ok(s"Scan for ${l.slug} / ${l.idAttendee} has been updated")
      }
      case None => {

        //1 add person
        ps.addPerson(Person(Some(l.idAttendee), Json.toJson(pj).toString))
        ls.addLead(Lead(l.slug, l.idAttendee, l.scanDateTime), leadNote)


        Ok(s"Scan for ${l.slug} / ${l.idAttendee} has been stored successfully")
      }
    }
  }

  private def buildNote(l: LeadGluon)

  = {
    l.message match {
      case "" => None
      case _ => Some(LeadNote(None, l.slug, l.idAttendee, l.message, l.scanDateTime))
    }
  }

  def addNote = {
    CORSAction {
      ApiAuthAction(parse.json) {
        implicit request =>

          implicit val leadReader: Reads[LeadFromRequest] = (
            (__ \ "idApplicant").read[String] and (__ \ "idTarget").read[String] and (__ \ "note")
              .readNullable[String]
            ) (LeadFromRequest.apply _)

          request.body.validate[LeadFromRequest].asEither match {
            case Left(errors) => BadRequest(toHateoas(ErrorMessage("Json_parsing_error", s"Json parsing throws an " +
              s"error ${
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
  }

  def readNotes(idAppliquant: String) = {
    CORSAction {
      ApiAuthAction {
        implicit request =>
          Ok(toHateoas(ls.getNotes(idAppliquant)))
      }
    }
  }

  def readNote(idNote: Long) = {
    CORSAction {
      ApiAuthAction {
        implicit request =>
          ls.getNote(idNote).fold(
            NotFound(toHateoas(ErrorMessage("Note_not_found", s"Note not found in request")))
          )(n => Ok(toHateoas(n)))

      }
    }
  }


  def leads = {
    CORSAction {
      ApiAuthAction {
        implicit request => {

          val regId = jsonUtils.extractRegIdFromTokenRequest(request)
          logger.info(s"regId found $regId")
          ls.getCompleteLeads(regId) match {
            case Nil => NotFound(toHateoas(ErrorMessage("leads_not_found", s"Leads for person with id ${regId} are " +
              s"not " +
              s"found")))
            case leads => Ok(toHateoas(leads))
          }
        }
      }


    }
  }

  def latestLeads(datetime: Long) = {
    CORSAction {
      ApiAuthAction {
        implicit request => {


          val regId = jsonUtils.extractRegIdFromTokenRequest(request)
          logger.info(s"regId found $regId")
          ls.getCompleteLatestLeads(regId, LocalDateTime.ofEpochSecond(datetime, 0, ZoneOffset.UTC)) match {
            case Nil => NotFound(toHateoas(ErrorMessage("leads_not_found", s"Leads for person with id ${regId} are " +
              s"not " +
              s"found")))
            case leads => Ok(toHateoas(leads))
          }
        }
      }


    }
  }

  private def convert2Lead(leadFromRequest: LeadFromRequest): Lead

  = {
    Lead(leadFromRequest.idApplicant, leadFromRequest.idTarget)
  }

  private def convert2LeadNote(leadFromRequest: LeadFromRequest): Option[LeadNote]

  = {
    leadFromRequest.note.map(n => LeadNote(None, leadFromRequest.idApplicant, leadFromRequest.idTarget, n))
  }


  private def sendNotification(item: (Lead, Option[LeadNote]))

  = {
    ns.addNotification(Notification(id = None,
      idRecipient = item._1.idTarget,
      idRequester = item._1.idApplicant,
      NotificationType.Connected.id.toLong,
      NotificationStatus.READ,
      LocalDateTime.now()))
  }


}
