package services

import model.Event
import play.api.db.Database
import repository.EventDAO

/**
  * Created by fsznajderman on 19/03/2017.
  */
trait EventService {

  def addEvent(event: Event): Unit

  def allEvents: Seq[Event]

}


class EventServiceImpl(db: Database) extends EventService {
  override def addEvent(event: Event): Unit = {
    db.withConnection { implicit c =>
      EventDAO.create(event)
    }
  }

  override def allEvents: Seq[Event] = {

    db.withConnection(implicit connection =>

      EventDAO.all

    )
  }
}
