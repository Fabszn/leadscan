package services

import anorm.NamedParameter
import dao.PersonDAO.Pass
import dao.{PersonDAO, PersonSensitiveDAO}
import model._
import play.api.db.Database
import play.api.libs.json.Json
import utils.LoggerAudit

/**
  * Created by fsznajderman on 20/01/2017.
  */


case class UpdatePerson(pString: Map[String, Option[String]] = Map(),
                        pInt: Map[String, Option[Int]] = Map(),
                        pBoolean: Map[String, Option[Boolean]] = Map()
                       )

trait PersonService {
  def getPerson(id: Long): Option[Person]

  def getCompletePerson(id: Long): Option[PersonJson]

  def getAllCompletePerson: Seq[PersonJson]

  def getPersonSensitive(id: Long): Option[PersonSensitive]

  def majPerson(id: Long, up: UpdatePerson)

  def addPerson(p: Person, token: String): Unit

  def addPersonSensitive(p: PersonSensitive): Unit

  def allPersons(): Seq[Person]

  def addRepresentative(firstname: String, lastname: String, email: String, company: String, title: String, token: String): PersonJson

  def pass: Seq[Pass]

  def addpass(regId: String, pass: String): Unit

}


class PersonServiceImpl(db: Database, ns: NotificationService, remote: RemoteClient, es: EventService) extends PersonService with LoggerAudit {


  override def allPersons(): Seq[Person] = {
    db.withConnection { implicit c =>
      PersonDAO.all
    }
  }


  override def getCompletePerson(id: Long): Option[PersonJson] = {

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

  override def getPerson(id: Long): Option[Person] = {
    db.withConnection { implicit c =>
      PersonDAO.find(id)
    }
  }


  override def getPersonSensitive(id: Long): Option[PersonSensitive] = {
    db.withConnection { implicit c =>
      //exprimer la distincion entre personne non trouvée et authorisation non donnée
      PersonDAO.find(id).filter(p => p.showSensitive).flatMap { _ =>
        PersonSensitiveDAO.getSensitiveDataByIdPerson(id)
      }
    }
  }

  override def majPerson(id: Long, up: UpdatePerson): Unit = {


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


  override def addPerson(p: Person, token: String): Unit = {
    db.withTransaction(implicit connexion =>

      PersonDAO.findBy(PersonDAO.pkField, p.id) match {
        case None => {
          es.addEvent(Event(typeEvent = ImportRegistration.typeEvent, message = s"Created  : ${p.id} - ${p.json}"))
          PersonDAO.create(p)
        }
        case Some(_) => {
          es.addEvent(Event(typeEvent = ImportRegistration.typeEvent, message = s"updated  : ${p.id} - ${p.json}"))
          PersonDAO.update(p)
        }
      }
    )


  }

  override def addPersonSensitive(p: PersonSensitive): Unit = {
    db.withConnection(implicit connexion =>

      PersonSensitiveDAO.findBy(PersonDAO.pkField, p.id) match {
        case None => PersonSensitiveDAO.create(p)
        case Some(_) => PersonSensitiveDAO.update(p)
      }
    )
  }

  override def addRepresentative(firstname: String, lastname: String, email: String, company: String, title: String, token: String): PersonJson =
    db.withTransaction(implicit connection => {

      val id = Some(generateId(email))
      val p = Person(id, firstname, lastname, "-", title, "-", 1, isTraining = false, showSensitive = true, 1)
      val ps = PersonSensitive(id, email, "-", company, "-", lookingForAJob = false)

      val pj = personToJson(p, ps)

      PersonDAO.create(p.copy(json = pj._2))
      PersonSensitiveDAO.create(ps)
      pj._1

    })


  private def personToJson(p: Person, ps: PersonSensitive): (PersonJson, String) = {

    import Person._

    val pj = PersonJson(generateId(ps.email).toString, p.firstname, p.lastname, ps.email, Option(ps.company), None, None, None, None, None, None, None, None, Option(p.position))
    (pj, Json.toJson(pj).toString)
  }

  private def generateId(email: String): Long = {
    email.toLowerCase.trim.hashCode.abs
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


