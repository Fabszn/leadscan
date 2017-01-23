package services

import anorm.NamedParameter
import dao.PersonDAO
import model.Person
import play.api.db.Database
import play.mvc.Http
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

  def majPerson[A](id: Long, up: UpdatePerson)
}


class PersonServiceImpl(db: Database) extends PersonService with LoggerAudit {

  override def getPerson(id: Long): Option[Person] = {
    db.withConnection { implicit c =>
      PersonDAO.find(id)
    }
  }

  override def majPerson[A](id: Long, up: UpdatePerson): Unit = {

    val params = (up.pString.map {
      case (k, ov) => NamedParameter(k, ov.get)
    } ++ up.pInt.map {
      case (k, ov) => NamedParameter(k, ov.get)
    } ++ up.pBoolean.map {
      case (k, ov) => NamedParameter(k, ov.get)
    }).toList
    logger.debug("r " + params)

    db.withConnection { implicit c =>
      PersonDAO.updateByNamedParameters(id)(params)
    }



  }
}


