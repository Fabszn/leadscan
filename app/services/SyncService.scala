package services

import dao.{LeadDAO, PersonDAO}
import model.{Event, Person, PersonJson, SynchMyDevoxx}
import play.api.db.Database
import play.api.libs.json.Json
import utils.LoggerAudit

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by fsznajderman on 16/04/2017.
  */
trait SyncService {

  def syncMyDevoxx(token: String): Unit

}


class SyncServiceImpl(remote: RemoteClient, es: EventService, db: Database) extends SyncService with LoggerAudit {
  override def syncMyDevoxx(token: String): Unit = {
    import model.Person._
    val leadScanPerson: Seq[PersonJson] = db.withConnection(implicit connection => {
      val scannedPersons = LeadDAO.uniqPersonScanned
      logger.info(s" Scanned persons ${scannedPersons.size}")
      scannedPersons
    }).map(p => Json.parse(p.json).as[PersonJson])


    leadScanPerson.foreach { localPerson => {
      logger.info(s"local ${localPerson.regId}")
      remote.loadByregId(localPerson.regId, token).map { remotePerson => {
        logger.info(s"remote ${remotePerson.registrantId} ")
        syncPerson(localPerson, remotePerson) match {
          case None => es.addEvent(Event(typeEvent = SynchMyDevoxx.typeEvent, message = s"NO UPDATE for regId : ${localPerson.regId}"))
          case Some(updatedPerson) =>

            db.withConnection(implicit connection => {
              PersonDAO.update(Person(Some(updatedPerson.regId), Json.toJson(updatedPerson).toString()))
              val msg =
                s"""
                   |RegId ${updatedPerson.regId} UPDATED
                   |Old => ${localPerson.toString}
                   |New => ${updatedPerson.toString}
            """.stripMargin

              es.addEvent(Event(typeEvent = SynchMyDevoxx.typeEvent, message = msg))
            })
        }

      }
      }
    }


    }


  }

  private def syncPerson(localPerson: PersonJson, remotePerson: MyDevoxxPerson): Option[PersonJson] = {

    //RegId|first_Name|last_Name|Email_Address|Company|Title
    if (localPerson.firstname.trim.toLowerCase != remotePerson.firstName.trim.toLowerCase ||
      localPerson.lastname.trim.toLowerCase != remotePerson.lastName.trim.toLowerCase ||
      localPerson.company.getOrElse("").trim.toLowerCase != remotePerson.company.trim.toLowerCase ||
      localPerson.title.getOrElse("").trim.toLowerCase != remotePerson.job.trim.toLowerCase) {
      logger.info("update")
      Some(localPerson.copy(firstname = remotePerson.firstName, lastname = remotePerson.lastName, company = Option(remotePerson.company), title = Option(remotePerson.job)))
    } else {
      logger.info("not update")
      None
    }


  }
}
