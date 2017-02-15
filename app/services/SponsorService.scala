package services

import dao.SponsorDAO
import dao.SponsorDAO.PersonSponsorInfo
import model.Sponsor
import play.api.db.Database

/**
  * Created by fsznajderman on 11/02/2017.
  */
trait SponsorService {
  def addSponsor(sponsor: Sponsor): Unit

  def loadSponsors(): Seq[Sponsor]

  def loadSponsor(id: Long): Option[Sponsor]

  def modifySponsor(sponsor: Sponsor): Unit

  def addRepresentative(idPerson: Long, idSpnsor: Long): Unit

  def removeRepresentative(idPerson: Long): Unit

  def LoadRepresentative(): Seq[PersonSponsorInfo]
}


class SponsorServiceImpl(db: Database) extends SponsorService {


  override def addRepresentative(idPerson: Long, idSponsor: Long): Unit =
    db.withConnection(implicit connection =>
      SponsorDAO.addRepresentative(idPerson, idSponsor)
    )

  override def modifySponsor(sponsor: Sponsor): Unit = {
    db.withConnection(implicit connection =>
      SponsorDAO.updateByNamedParameters(sponsor.id.get)(SponsorDAO.getParams(sponsor).toList)
    )
  }

  override def addSponsor(sponsor: Sponsor): Unit = {
    db.withConnection(implicit connection =>
      SponsorDAO.create(sponsor)

    )
  }


  override def loadSponsors(): Seq[Sponsor] = {
    db.withConnection(implicit connection =>
      SponsorDAO.all
    )

  }

  override def loadSponsor(id: Long): Option[Sponsor] = {
    db.withConnection(implicit connection =>
      SponsorDAO.findBy("id", id)
    )

  }

  override def LoadRepresentative(): Seq[PersonSponsorInfo] = {
    db.withConnection(implicit connection =>
      SponsorDAO.allWithSponsor
    )
  }

  override def removeRepresentative(idPerson: Long): Unit = {
    db.withConnection(implicit connection =>
      SponsorDAO.deleteRepresentative(idPerson)
    )

  }
}
