package repository

import java.sql.Connection
import java.time.LocalDateTime

import anorm.SqlParser.get
import anorm.{NamedParameter, RowParser, _}
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


  def all(implicit c: Connection): Seq[Event] = {

    SQL"""
         select * from events
       """.as(rowParser *)
  }

}
