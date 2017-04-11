package services

import config.Settings
import dao.SponsorDAO.PersonSponsorInfo
import dao.{LeadNoteDAO, PersonDAO, SponsorDAO}
import model.{PersonJson, Sponsor}
import org.apache.commons.lang3.StringUtils
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

  def addRepresentative(idPerson: String, idSponsor: Long): Unit

  def removeRepresentative(idPerson: String): Unit

  def loadRepresentative(): Seq[PersonSponsorInfo]

  def loadOnlyRepresentative(idSponsor: Long): Seq[PersonSponsorInfo]

  def loadOnlyRepresentative: Seq[PersonSponsorInfo]

  def exportForSponsor(id: Long): Seq[String]

  def exportForEvent: Seq[String]

  def exportForRepresentative(id: String): Seq[String]

  def isRepresentative(idPerson: String, idSponsor: Long): Boolean

  def loadSponsorFromRepresentative(id: String): Option[Sponsor]


}


class SponsorServiceImpl(db: Database, es: EventService) extends SponsorService {


  val SEP = "|"
  val SEP_COMMA = ","

  override def addRepresentative(idPerson: String, idSponsor: Long): Unit =
    db.withConnection(implicit connection =>
      SponsorDAO.addRepresentative(idPerson, idSponsor)

    )


  override def isRepresentative(idPerson: String, idSponsor: Long): Boolean = {
    db.withConnection(implicit connection =>
      SponsorDAO.isRepresentative(idPerson, idSponsor)
    )
  }

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

  override def removeRepresentative(idPerson: String): Unit = {
    db.withConnection(implicit connection =>
      SponsorDAO.deleteRepresentative(idPerson)
    )

  }


  override def loadSponsor(sponsorName: String): Option[Sponsor] = {
    db.withConnection(implicit connection =>
      SponsorDAO.findBy("name", StringUtils.trimToEmpty(sponsorName).toUpperCase)
    )

  }

  override def exportForSponsor(id: Long): Seq[String] = {


    import model.Person._
    Settings.headersSponsor :: db.withConnection(implicit connection =>
      SponsorDAO.personBySponsor(id).map((line: SponsorDAO.LeadLine) => {

        Json.parse(line.json).validate[PersonJson].asEither match {
          case Left(error) => s"An error occurred wiht this line -> $error"
          case Right(pj) => {


            val applicant: Option[(String, String)] = PersonDAO.find(line.idApplicant).map(p => {
              import model.Person.personJsonReader
              val app = Json.parse(p.json).as[PersonJson]
              (app.firstname, app.lastname)
            })

            val notes = LeadNoteDAO.findNoteByApplicantAndTarget(line.idApplicant, pj.regId)

            //todo must be fixed
            val nbNote = if (notes.isEmpty) 0 else 1
            val notesVal = notes.map(n => n.note).mkString(" ")
            //headers.sponsor = "Rep_first_Name,Rep_last_Name,RegId,first_Name,last_Name,Email_Address,Company,Country,Title,nbNote,allNotes"
            s"""${applicant.get._1}$SEP${applicant.get._2}$SEP${pj.regId}$SEP${pj.firstname}$SEP${pj.lastname}$SEP${pj.email}$SEP${pj.company.getOrElse("")}$SEP${pj.title.getOrElse("")}$SEP $notesVal"""
          }
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

            val applicant = PersonDAO.find(line.idApplicant).map(p => {
              import model.Person.personJsonReader
              val app = Json.parse(p.json).as[PersonJson]
              (app.firstname, app.lastname)
            })


            val notes = LeadNoteDAO.findNoteByApplicantAndTarget(line.idApplicant, pj.regId)

            //val nbNote = notes.count(n => n.note.trim.nonEmpty)

            s"""${applicant.get._1}$SEP${applicant.get._2}$SEP${pj.regId}$SEP${pj.firstname}$SEP${pj.lastname}$SEP${pj.email}$SEP${pj.company.getOrElse("")}$SEP${pj.title.getOrElse("")}"""
        }
      })
    ).toList
  }

  override def exportForRepresentative(id: String): Seq[String] = {

    import model.Person._
    Settings.headersRepresentative :: db.withConnection(implicit connection =>
      SponsorDAO.personByRepresentative(id).map(line => {

        Json.parse(line.json).validate[PersonJson].asEither match {
          case Left(error) => s"An error occurred wiht this line -> $error"
          case Right(pj) =>
            clean(s"""${pj.regId}$SEP${pj.firstname}$SEP${pj.lastname}$SEP${pj.email}$SEP${pj.phone.getOrElse("")}$SEP${pj.title.getOrElse("")}$SEP${line.note}""")
        }
      })
    ).toList


  }


  override def loadSponsorFromRepresentative(idRepresentative: String): Option[Sponsor] = {

    db.withConnection(implicit connection =>
      SponsorDAO.isRepresentative(idRepresentative)
    )
  }

  private def clean(chaine: String): String = {

    chaine.replace(SEP_COMMA, ".").replace(SEP, SEP_COMMA)
  }
}




