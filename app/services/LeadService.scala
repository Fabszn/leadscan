package services

import java.time.LocalDateTime

import dao.{LeadDAO, LeadNoteDAO, PersonDAO}
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

  def isExists(idTarget:Long): Option[Person]

  def getLeads(id: Long): Seq[CompletePerson]

  def getCompleteLeads(id: Long): Seq[CompletePersonWithNotes]

  def getNotes(idPerson: Long): Seq[LeadNote]

  def getNote(idNote: Long): Option[LeadNote]

}


class LeadServiceImpl(db: Database) extends LeadService with LoggerAudit {


  override def getCompleteLeads(id: Long): Seq[CompletePersonWithNotes] = {
    val allNotes = this.getNotes(id)
    this.getLeads(id).map(cp => CompletePersonWithNotes(cp, allNotes.filter(ln => {
      ln.idTarget.equals(cp.id.get)
    })))


  }

  override def getNote(idNote: Long): Option[LeadNote] = {

    db.withConnection { implicit c =>
      LeadNoteDAO.findBy("id", idNote)
    }


  }

  override def getNotes(id: Long): Seq[LeadNote] = {

    db.withConnection { implicit c =>
      LeadNoteDAO.listBy("idapplicant", id)
    }

  }

  override def addNote(note: LeadNote): Unit = {
    db.withConnection { implicit c =>
      LeadNoteDAO.updateNote(note)
    }
  }

  override def isAlreadyConnect(contact: Lead): Option[Lead] = {
    db.withConnection { implicit c =>
      LeadDAO.findByPks(contact.idApplicant, contact.idTarget)
    }
  }

  override def addLead(contact: Lead, leadNote: Option[LeadNote]): Unit = {
    db.withTransaction { implicit c =>
      LeadDAO.create(contact)
      val note = leadNote match {
        case Some(n) => n
        case None => LeadNote(None, contact.idApplicant, contact.idTarget, "", LocalDateTime.now())
      }
      LeadNoteDAO.create(note)
    }
  }

  override def getLeads(id: Long): Seq[CompletePerson] = {
    db.withConnection { implicit c =>
      PersonDAO.findAllLeadById(id)
    }

  }

  override def isExists(idTarget: Long): Option[Person] = {
    db.withConnection { implicit c =>
      PersonDAO.findBy(PersonDAO.pkField, idTarget)
    }

  }
}