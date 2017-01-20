package services

import dao.PersonDAO
import model.Person
import play.api.db.Database

/**
  * Created by fsznajderman on 20/01/2017.
  */


trait PersonService {

  def getPerson(id: Long): Option[Person]

}


class PersonServiceImpl(db: Database) extends PersonService {

  override def getPerson(id: Long): Option[Person] = {
    db.withConnection { implicit c =>
      PersonDAO.find(id)
    }
  }

}


