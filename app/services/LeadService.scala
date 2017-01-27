package services

import dao.{LeadDAO, PersonDAO}
import model.{Lead, Person}
import play.api.db.Database

/**
  * Created by fsznajderman on 24/01/2017.
  */
trait LeadService {

  def addLead(contact: Lead)

  def isAlreadyConnect(contact: Lead): Option[Lead]

  def getLeads(id: Long): Seq[Person]


}


class LeadServiceImpl(db: Database) extends LeadService {


  override def isAlreadyConnect(contact: Lead): Option[Lead] = {
    db.withConnection { implicit c =>
      LeadDAO.findByPks(contact.idApplicant, contact.idTarget)
    }
  }

  override def addLead(contact: Lead): Unit = {
    db.withConnection { implicit c =>
      LeadDAO.create(contact)
    }
  }

  override def getLeads(id: Long): Seq[Person] = {
    db.withConnection { implicit c =>
      PersonDAO.findAllLeadById(id)
    }

  }

}