package services

import anorm.NamedParameter
import dao.{PersonDAO, PersonSensitiveDAO}
import model.{CompletePerson, Person, PersonJson, PersonSensitive}
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

  def getCompletePerson(id: Long): Option[CompletePerson]

  def getPersonSensitive(id: Long): Option[PersonSensitive]

  def majPerson(id: Long, up: UpdatePerson)

  def addPerson(p: Person): Unit

  def addPersonSensitive(p: PersonSensitive): Unit

  def allPersons(): Seq[Person]

  def addRepresentative(firstname: String, lastname: String, email: String, company: String, title: String): PersonJson

}


class PersonServiceImpl(db: Database, ns: NotificationService) extends PersonService with LoggerAudit {


  override def allPersons(): Seq[Person] = {
    db.withConnection { implicit c =>
      PersonDAO.all
    }
  }


  override def getCompletePerson(id: Long): Option[CompletePerson] = {

    db.withConnection { implicit c =>
      PersonDAO.loadCompletePerson(id)
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


  override def addPerson(p: Person): Unit = {
    db.withTransaction(implicit connexion =>

      PersonDAO.findBy(PersonDAO.pkField, p.id) match {
        case None => {
          logger.debug(s"create $p")
          PersonDAO.create(p)
          ns.sendMail(Seq("fabszn@gmail.com", "nmartignole@gmail.com"))
        }
        case Some(_) => {

          logger.debug(s"update $p")
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

  override def addRepresentative(firstname: String, lastname: String, email: String, company: String, title: String): PersonJson =
    db.withTransaction(implicit connection => {

      val id = Some(generateId(email))
      val p = Person(id, firstname, lastname, "-", title, "-", 1, isTraining = false, showSensitive = true, 1)
      val ps = PersonSensitive(id, email, "-", company, "-", lookingForAJob = false)

      val pj = personToJson(p, ps)

      PersonDAO.create(p.copy(json = pj._2))
      PersonSensitiveDAO.create(ps)
      ns.sendMail(Seq("fabszn@gmail.com", "nmartignole@gmail.com"))
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


}


