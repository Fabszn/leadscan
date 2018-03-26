package services

import config.Settings
import repository.SponsorDAO.PersonSponsorInfo
import repository.{LeadNoteDAO, PersonDAO, SponsorDAO}
import model._
import org.apache.commons.lang3.StringUtils
import play.api.db.Database
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import utils.LoggerAudit

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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

  def loadScannedPersonBySponsor(id: Long): Seq[PersonJson]

  def loadRemoteSponsorsList: Unit


}


class SponsorServiceImpl(
  db: Database,
  es: EventService,
  ws: WSClient,
  ps: PersonService)
  extends SponsorService with LoggerAudit {


  val SEP = "|"
  val SEP_COMMA = ","

  override def addRepresentative(idPerson: String, idSponsor: Long): Unit = {
    db.withConnection(implicit connection =>
      SponsorDAO.addRepresentative(idPerson, idSponsor)

    )
  }


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
            //val nbNote = if (notes.isEmpty) 0 else 1
            val notesVal = notes.map(n => n.note.replace("\n", " ")).mkString(" ")

            //RegId,gender,firstname,lastname,email,title,company,workAdress1,workAdress2,city,workCounty,
            // workPostCode,workCountry,phone
            s"""${applicant.get._1}$SEP${applicant.get._2}$SEP${pj.regId}$SEP${pj.gender.getOrElse("")}$SEP${
              pj
                .firstname
            }$SEP${pj.lastname}$SEP${pj.email}$SEP${pj.title}$SEP${pj.company}$SEP${
              pj
                .workAdress1.getOrElse("")
            }$SEP${pj.workAdress2.getOrElse("")}$SEP${pj.city.getOrElse("")}$SEP${
              pj
                .workCounty.getOrElse("")
            }$SEP${pj.WorkPostCode.getOrElse("")}$SEP${
              pj.workCountry.getOrElse("")
            }$SEP${pj.phone.getOrElse("")}$SEP $notesVal"""
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


            //val notes = LeadNoteDAO.findNoteByApplicantAndTarget(line.idApplicant, pj.regId)

            //val nbNote = notes.count(n => n.note.trim.nonEmpty)

            s"""${applicant.get._1}$SEP${applicant.get._2}$SEP${pj.regId}$SEP${pj.gender.getOrElse("")}$SEP${
              pj
                .firstname
            }$SEP${pj.lastname}$SEP${pj.email}$SEP${pj.title}$SEP${pj.company}$SEP${
              pj
                .workAdress1.getOrElse("")
            }$SEP${pj.workAdress2.getOrElse("")}$SEP${pj.city.getOrElse("")}$SEP${
              pj
                .workCounty.getOrElse("")
            }$SEP${pj.WorkPostCode.getOrElse("")}$SEP${
              pj.workCountry.getOrElse("")
            }$SEP${pj.phone.getOrElse("")}"""
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
            clean(
              s"""${pj.regId}$SEP${pj.firstname}$SEP${pj.lastname}$SEP${pj.email}$SEP${
                pj.phone.getOrElse("")
              }$SEP${pj.title}$SEP${line.note}""")
        }
      })
    ).toList


  }


  override def loadScannedPersonBySponsor(id: Long): Seq[PersonJson] = {
    db.withConnection(implicit connection => {
      import model.Person._
      SponsorDAO.allPersonScannedBySponsor(id).map(line =>
        Json.parse(line.json).validate[PersonJson].asEither match {
          case Right(pj) => pj
        }
      )
    }
    )
  }

  override def loadSponsorFromRepresentative(idRepresentative: String): Option[Sponsor] = {

    db.withConnection(implicit connection =>
      SponsorDAO.isRepresentative(idRepresentative)
    )
  }


  override def loadRemoteSponsorsList: Unit = {

    import scala.concurrent.ExecutionContext.Implicits.global


    for {
      json <- ws.url(Settings.listSponsorsUrl)
        .withHeaders("Content-Type" -> "application/json", "Accept" -> "application/json")
        .get().map(w => {
        w.json

      })
    } yield {
      val sponsors = Try {
        (json \ "events").as[Seq[EventDevoxx]].filter(e => e.slug == "dvxfr18").head.sponsors
          .map(s => {
            logger.info(s"Sponsor ${s}")
            Sponsor(None, s.slug, s.name, s.level)
          }
          )
      } match {
        case Success(e) => e
        case Failure(s) => {
          logger.error(s.getMessage)
          throw new Exception(s)
        }
      }

      logger.info(s"nb sponsors found ${sponsors.size}")
      Try {

          sponsors.foreach(s => {
            logger.info(s"current ${s}")
            db.withConnection(implicit c => {
            SponsorDAO.findBy("slug", s.slug) match {
              case Some(sponsorFound) => {
                es.addEvent(Event(typeEvent = "update Sponsor", message = s"Sponsor ${s.slug} has been updated"))
                SponsorDAO.update(sponsorFound.copy(name = s.name, level = s.level))
              }
              case None => {
                es.addEvent(Event(typeEvent = "create Sponsor", message = s"Sponsor ${s.slug} has been created"))
                SponsorDAO.create(s)
                ps.addPerson(Person(Some(s.slug), Json.toJson(PersonJson.fakeSponsorPerson(s)).toString))
                addRepresentative(s.slug, SponsorDAO.findBy("slug", s.slug).get.id.get)
                logger.info(s"Sponsor ${s} created")
              }
            }
          }
          )
        })
      } match {
        case Success(e) => e
        case Failure(s) => {
          s.printStackTrace
          logger.error(s.getMessage)
          throw new Exception(s)
        }
      }
    }
  }


  private def clean(chaine: String): String = {

    chaine.replace(SEP_COMMA, ".").replace(SEP, SEP_COMMA)
  }
}




