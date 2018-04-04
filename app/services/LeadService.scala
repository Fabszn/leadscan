package services

import java.time.LocalDateTime

import repository.{LeadDAO, LeadNoteDAO, PersonDAO}
import model._
import play.api.db.Database
import utils.LoggerAudit

/**
 * Created by fsznajderman on 24/01/2017.
 */
trait LeadService {

  def addLead(contact: Lead, note: Option[LeadNote]): Unit

  def addNote(note: LeadNote): Unit

  def isAlreadyConnect(contact: Lead): Option[Lead]

  def isExists(idTarget: String): Option[Person]

  def getLeads(id: String): Seq[CompletePerson]

  def getLatestLeads(id: String, datetime: LocalDateTime): Seq[CompletePerson]

  def getCompleteLeads(id: String): Seq[CompletePersonWithNotes]

  def getCompleteLatestLeads(id: String, dateTime: LocalDateTime): Seq[CompletePersonWithNotes]

  def getNotes(idPerson: String): Seq[LeadNote]

  def getNote(idNote: Long): Option[LeadNote]

  def getNote(slug: String, idAttendee: String): Option[LeadNote]


  def deleteLead(leadIds: LeadIDs)

}


class LeadServiceImpl(db: Database)(implicit es: EventService) extends LeadService with LoggerAudit {


  override def deleteLead(leadIds: LeadIDs) = {

    db.withTransaction { implicit c =>
      LeadDAO.deleteLead(leadIds)
      LeadNoteDAO.deleteLeadNote(leadIds)
      es.addEvent(Event(None, DeleteLead.typeEvent, s"lead $leadIds has been deleted"))
    }

  }


  override def getNote(slug: String, idAttendee: String): Option[LeadNote] = {
    db.withConnection { implicit c =>
      LeadNoteDAO.findNoteByApplicantAndTarget(slug, idAttendee).headOption
    }
  }

  override def getCompleteLeads(id: String): Seq[CompletePersonWithNotes]

  = {
    val allNotes = this.getNotes(id)
    this.getLeads(id).map(cp => CompletePersonWithNotes(cp, allNotes.filter(ln => {
      ln.idTarget.equals(cp.regId)
    })))


  }

  override def getCompleteLatestLeads(id: String, datetime: LocalDateTime): Seq[CompletePersonWithNotes]  = {
    val allNotes = this.getNotes(id)
    this.getLatestLeads(id, datetime).map(cp => CompletePersonWithNotes(cp, allNotes.filter(ln => {
      ln.idTarget.equals(cp.regId)
    })))


  }

  override def getNote(idNote: Long): Option[LeadNote]  = {

    db.withConnection { implicit c =>
      LeadNoteDAO.findBy("id", idNote)
    }


  }

  override def getNotes(id: String): Seq[LeadNote]  = {

    db.withConnection { implicit c =>
      LeadNoteDAO.listBy("idapplicant", id)
    }

  }

  override def addNote(note: LeadNote): Unit  = {
    db.withTransaction { implicit c =>
      LeadNoteDAO.updateNote(note)
      es.addEvent(Event(None, UpdateLeadNote.typeEvent, s"lead $note has been updated"))
    }
  }

  override def isAlreadyConnect(contact: Lead): Option[Lead]  = {
    db.withConnection { implicit c =>
      LeadDAO.findByPks(contact.idApplicant, contact.idTarget)
    }
  }

  override def addLead(contact: Lead, leadNote: Option[LeadNote]): Unit  = {
    db.withTransaction { implicit c =>
      LeadDAO.create(contact)
      val note = leadNote match {
        case Some(n) => n
        case None => LeadNote(None, contact.idApplicant, contact.idTarget, "", LocalDateTime.now())
      }
      LeadNoteDAO.create(note)
      es.addEvent(Event(typeEvent = CreateLead.typeEvent, message = s"Created lead : Sponsor : ${contact.idApplicant} - Attendee : ${contact.idTarget}"))
    }
  }

  override def getLeads(id: String): Seq[CompletePerson]  = {
    db.withConnection { implicit c =>
      PersonDAO.findAllLeadById(id)
    }

  }

  override def getLatestLeads(id: String, datetime: LocalDateTime): Seq[CompletePerson]  = {
    db.withConnection { implicit c =>
      PersonDAO.findAllLatestLeadById(id, datetime)
    }

  }

  override def isExists(idTarget: String): Option[Person]  = {
    db.withConnection { implicit c =>
      PersonDAO.findBy(PersonDAO.pkField, idTarget)
    }

  }
}