package services

import anorm.NamedParameter
import repository.PersonDAO
import repository.PersonDAO.Pass
import model._
import play.api.db.Database
import play.api.libs.json.Json
import utils.LoggerAudit

/**
 * Created by fsznajderman on 20/01/2017.
 */


case class UpdatePerson(
  pString: Map[String, Option[String]] = Map(),
  pInt: Map[String, Option[Int]] = Map(),
  pBoolean: Map[String, Option[Boolean]] = Map()
)

trait PersonService {
  def findByEmail(email: String): Option[Person]

  def getPerson(id: String): Option[Person]

  def getCompletePerson(id: String): Option[PersonJson]

  def getAllCompletePerson: Seq[PersonJson]

  def majPerson(id: String, up: UpdatePerson)

  def addPerson(p: Person): Unit

  def allPersons(): Seq[Person]

  def addRepresentative(firstname: String, lastname: String, email: String, company: String, title: String): PersonJson

  def pass: Seq[Pass]

  def addpass(regId: String, pass: String): Unit

}


class PersonServiceImpl(db: Database, ns: NotificationService, remote: RemoteClient, es: EventService)
  extends PersonService with LoggerAudit {


  override def allPersons(): Seq[Person] = {
    db.withConnection { implicit c =>
      PersonDAO.all
    }
  }


  override def getCompletePerson(id: String): Option[PersonJson] = {

    db.withConnection { implicit c =>
      logger.debug("completePersons")
      getPerson(id).map {
        p => Person.json2PersonJson(p.json)
      }
    }

  }


  override def getAllCompletePerson: Seq[PersonJson] = {

    db.withConnection { implicit c =>
      logger.debug("allcompletePersons")
      allPersons.map {
        p => Person.json2PersonJson(p.json)
      }
    }
  }

  override def getPerson(id: String): Option[Person] = {
    db.withConnection { implicit c =>
      PersonDAO.find(id)
    }
  }

  override def findByEmail(email: String): Option[Person] = {
    db.withConnection { implicit c =>
      PersonDAO.findBy[String]("email", email)
    }
  }


  override def majPerson(id: String, up: UpdatePerson): Unit = {


    val params = (up.pString.map {
      case (k, ov) => NamedParameter(k, ov.get)
    } ++ up.pInt.map {
      case (k, ov) => NamedParameter(k, ov.get)
    } ++ up.pBoolean.map {
      case (k, ov) => NamedParameter(k, ov.get)
    }).toList

    db.withConnection { implicit c =>
      PersonDAO.updateByNamedParameters(id)(params)
    }
  }


  override def addPerson(p: Person): Unit = {
    db.withTransaction(implicit connexion => {

      val action = PersonDAO.findBy(PersonDAO.pkField, p.id) match {
        case None => {
          PersonDAO.create(p)
          "Created"
        }
        case Some(_) => {
          PersonDAO.update(p)
          "Updated"
        }
      }
      es.addEvent(Event(typeEvent = ImportRegistration.typeEvent, message = s"$action  : ${p.id} - ${p.json}"))
    })
  }


  override def addRepresentative(firstname: String, lastname: String, email: String, company: String, title: String)
  : PersonJson = {
    db.withTransaction(implicit connection => {


      import Person._
      val id = Some(generateId(email))


      val pj = PersonJson(generateId(email).toString, None, firstname, lastname, email, title, company, None, None,
        None, None, None, None, None)
      (pj, Json.toJson(pj).toString)


      PersonDAO.create(Person(Option(pj.regId), Json.toJson(pj).toString))

      pj

    })
  }


  private def generateId(email: String): String = {
    s"${email.toLowerCase.trim.hashCode.abs}"
  }


  override def addpass(regId: String, pass: String): Unit = {
    db.withConnection(implicit connexion =>

      PersonDAO.addPass(regId, pass)
    )

  }

  override def pass: Seq[Pass] = {
    db.withConnection(implicit connexion =>

      PersonDAO.pass
    )

  }
}


