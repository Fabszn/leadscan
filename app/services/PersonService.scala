package services

import anorm.NamedParameter
import dao.{PersonDAO, PersonSensitiveDAO}
import model.{Person, PersonSensitive}
import play.api.db.Database
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

  def getPersonSensitive(id: Long): Option[PersonSensitive]

  def majPerson(id: Long, up: UpdatePerson)
}


class PersonServiceImpl(db: Database) extends PersonService with LoggerAudit {

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
}


