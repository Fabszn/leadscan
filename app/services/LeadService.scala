package services

import dao.LeadDAO
import model.Lead
import play.api.db.Database

/**
  * Created by fsznajderman on 24/01/2017.
  */
trait LeadService {

  def addLead(contact: Lead)

  def isAlreadyConnect(contact: Lead): Option[Lead]


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
}