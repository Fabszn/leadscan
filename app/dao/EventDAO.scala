package dao

import java.time.LocalDateTime

import anorm.SqlParser.get
import anorm.{NamedParameter, RowParser}
import model.Event

/**
  * Created by fsznajderman on 19/03/2017.
  */
object EventDAO extends mainDBDAO[Event, Long] {
  override def rowParser: RowParser[Event] = for {
    id <- get[Option[Long]]("id")
    typeEvent <- get[String]("type")
    message <- get[String]("message")
    eventdateTime <- get[LocalDateTime]("dateTime")
  } yield Event(id, typeEvent, message, eventdateTime)


  override def table: String = "events"

  override def getParams(item: Event): Seq[NamedParameter] = Seq[NamedParameter](
    'type -> item.typeEvent,
    'message -> item.message,
    'dateTime -> item.datetime
  )
}
