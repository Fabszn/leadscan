package services

import config.Settings
import dao.SponsorDAO.PersonSponsorInfo
import dao.{LeadNoteDAO, PersonDAO, SponsorDAO}
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

  def loadSponsor(name: String): Option[Sponsor]

  def modifySponsor(sponsor: Sponsor): Unit

  def addRepresentative(idPerson: Long, idSpnsor: Long): Unit

  def removeRepresentative(idPerson: Long): Unit

  def loadRepresentative(): Seq[PersonSponsorInfo]

  def loadOnlyRepresentative(idSponsor: Long): Seq[PersonSponsorInfo]

  def loadOnlyRepresentative: Seq[PersonSponsorInfo]

  def exportForSponsor(id: Long): Seq[String]

  def exportForEvent: Seq[String]

  def exportForRepresentative(id: Long): Seq[String]


}


class SponsorServiceImpl(db: Database, es: EventService) extends SponsorService {


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

  override def loadRepresentative(): Seq[PersonSponsorInfo] = {
    db.withConnection(implicit connection =>
      SponsorDAO.allWithSponsor
    )
  }

  override def loadOnlyRepresentative(idSponsor: Long): Seq[PersonSponsorInfo] = {
    db.withConnection(implicit connection =>
      SponsorDAO.onlyRepresentatives(idSponsor)
    )
  }

  override def loadOnlyRepresentative: Seq[PersonSponsorInfo] = {
    db.withConnection(implicit connection =>
      SponsorDAO.onlyRepresentatives
    )
  }

  override def removeRepresentative(idPerson: Long): Unit = {
    db.withConnection(implicit connection =>
      SponsorDAO.deleteRepresentative(idPerson)
    )

  }


  override def loadSponsor(sponsorName: String): Option[Sponsor] = {

    db.withConnection(implicit connection =>
      SponsorDAO.findBy("name", sponsorName)
    )

  }

  override def exportForSponsor(id: Long): Seq[String] = {


    import model.Person._
    Settings.headersSponsor :: db.withConnection(implicit connection =>
      SponsorDAO.personBySponsor(id).map(line => {

        Json.parse(line.json).validate[PersonJson].asEither match {
          case Left(error) => s"An error occurred wiht this line -> $error"
          case Right(pj) =>

            val applicant = PersonDAO.find(line.idApplicant).map(p => (p.firstname, p.lastname)).getOrElse(("not_found", "not_found"))


            val notes = LeadNoteDAO.findNoteByApplicantAndTarget(line.idApplicant, pj.regId.toLong)

            //todo must be fixed
            val nbNote = if (notes.isEmpty) 0 else 1
            val notesVal = notes.map(n => n.note).mkString(" ")

            s"""${applicant._1}|${applicant._2}|${pj.regId}|${pj.firstname}|${pj.lastname}|${pj.email}|${pj.country.getOrElse("")}|${pj.phone.getOrElse("")}|${pj.title.getOrElse("")}|$nbNote| $notesVal"""
        }
      })
    ).toList

  }

  override def exportForEvent: Seq[String] = {

    import model.Person._

    Settings.headersEvent :: db.withConnection(implicit connection =>
      SponsorDAO.allPersonScanned.map(line => {

        Json.parse(line.json).validate[PersonJson].asEither match {
          case Left(error) => s"An error occurred wiht this line -> $error"
          case Right(pj) =>

            val applicant = PersonDAO.find(line.idApplicant).map(p => (p.firstname, p.lastname)).getOrElse(("not_found", "not_found"))


            val notes = LeadNoteDAO.findNoteByApplicantAndTarget(line.idApplicant, pj.regId.toLong)

            val nbNote = notes.count(n => n.note.trim.nonEmpty)

            s"""${applicant._1}|${applicant._2}|${line.sponsor}|${pj.regId}|${pj.firstname}|${pj.lastname}|${pj.email}|${pj.country.getOrElse("")}|${pj.phone.getOrElse("")}|${pj.title.getOrElse("")}|$nbNote"""
        }
      })
    ).toList
  }

  override def exportForRepresentative(id: Long): Seq[String] = {

    import model.Person._
    Settings.headersRepresentative :: db.withConnection(implicit connection =>
      SponsorDAO.personByRepresentative(id).map(line => {

        Json.parse(line.json).validate[PersonJson].asEither match {
          case Left(error) => s"An error occurred wiht this line -> $error"
          case Right(pj) =>

            //val applicant = PersonDAO.find(line.idApplicant).map(p => (p.firstname, p.lastname)).getOrElse(("not_found", "not_found"))


            //val notes = LeadNoteDAO.findNoteByApplicantAndTarget(line.idApplicant, pj.regId.toLong)

            //todo must be fixed
            //val nbNote = if (notes.isEmpty) 0 else 1
            //val notesVal = notes.map(n => n.note).mkString(" ")

            s"""${pj.regId}|${pj.firstname}|${pj.lastname}|${pj.email}|${pj.country.getOrElse("")}|${pj.phone.getOrElse("")}|${pj.title.getOrElse("")}| ${line.note}"""
        }
      })
    ).toList


  }
}




