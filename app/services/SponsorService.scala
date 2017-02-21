package services

import config.Settings
import dao.SponsorDAO.PersonSponsorInfo
import dao.{LeadNoteDAO, SponsorDAO}
import model.{PersonJson, Sponsor}
import play.api.db.Database
import play.api.libs.json.Json

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

  def export(id: Long): Seq[String]
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

  override def export(id: Long): Seq[String] = {

    import model.Person._
    Settings.headers :: db.withConnection(implicit connection =>
      SponsorDAO.personBySponsor(id).map(line => {

        Json.parse(line.json).validate[PersonJson].asEither match {
          case Left(error) => s"An error occurred wiht this line -> $error"
          case Right(pj) =>
            val notes = LeadNoteDAO.findNoteByApplicantAndTarget(line.idApplicant, pj.regId.toLong)

            val nbNote = notes.size
            val notesVal = notes.map(n => n.note).mkString("|")

            //get number of note
            // make makstring and concat at the end

            s"""${pj.regId}|${pj.firstname}|${pj.lastname}|${pj.email}|${pj.company.getOrElse("")}|${pj.address1.getOrElse("")}|${pj.address2.getOrElse("")}|${pj.city.getOrElse("")}|${pj.region.getOrElse("")}|${pj.postalCode.getOrElse("")}|${pj.country.getOrElse("")}|${pj.phone.getOrElse("")}|${pj.fax.getOrElse("")}|${pj.title.getOrElse("")}|$nbNote| $notesVal"""
        }
      })
    ).toList


  }


}




