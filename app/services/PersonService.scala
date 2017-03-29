package services

import anorm.NamedParameter
import dao.PersonDAO
import dao.PersonDAO.Pass
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
  def getPerson(id: String): Option[Person]

  def getCompletePerson(id: String): Option[PersonJson]

  def getAllCompletePerson: Seq[PersonJson]

  def majPerson(id: String, up: UpdatePerson)

  def addPerson(p: Person, token: String): Unit

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


  override def addRepresentative(firstname: String, lastname: String, email: String, company: String, title: String, token: String): PersonJson =
    db.withTransaction(implicit connection => {


      import Person._
      val id = Some(generateId(email))


      val pj = PersonJson(generateId(email).toString, firstname, lastname, None, email, Option(company), None, None, None, "false", None, None, "true"  )
      (pj, Json.toJson(pj).toString)


      PersonDAO.create(Person(Option(pj.regId), Json.toJson(pj).toString))

      pj

    })


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


