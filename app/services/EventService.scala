package services

import dao.EventDAO
import model.Event
import play.api.db.Database

/**
  * Created by fsznajderman on 19/03/2017.
  */
trait EventService {

  def addEvent(event: Event): Unit

}


class EventServiceImpl(db: Database) extends EventService {
  override def addEvent(event: Event): Unit = {
    db.withConnection { implicit c =>
      EventDAO.create(event)
    }
  }
}
