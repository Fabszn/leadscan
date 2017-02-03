package services

import dao.{LeadDAO, LeadNoteDAO, PersonDAO}
import model.{Lead, LeadNote, Person}
import play.api.db.Database

/**
  * Created by fsznajderman on 24/01/2017.
  */
trait LeadService {

  def addLead(contact: Lead, note: Option[LeadNote]):Unit

  def addNote(note: LeadNote):Unit

  def isAlreadyConnect(contact: Lead): Option[Lead]

  def getLeads(id: Long): Seq[Person]


}


class LeadServiceImpl(db: Database) extends LeadService {


  override def addNote(note: LeadNote): Unit = {
    db.withConnection { implicit c =>
      LeadNoteDAO.create(note)
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
      for {
        note <- leadNote
      } yield {
        LeadNoteDAO.create(note)
      }

    }
  }

  override def getLeads(id: Long): Seq[Person] = {
    db.withConnection { implicit c =>
      PersonDAO.findAllLeadById(id)
    }

  }

}