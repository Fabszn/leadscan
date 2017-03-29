package dao

import java.sql.Connection
import java.time.LocalDateTime

import anorm.SqlParser.get
import anorm.{NamedParameter, RowParser, _}
import model.{Notification, NotificationStatus}


/**
  * Created by fsznajderman on 29/01/2017.
  */
object NotificationDAO extends mainDBDAO[Notification, Long] {
  override def rowParser: RowParser[Notification] = for {
    id <- get[Option[Long]]("id")
    idRecipient <- get[String]("idRecipient")
    idRequester <- get[String]("idRequester")
    typeNotif <- get[Long]("idType")
    statusNotif <- get[Long]("idStatus")
    dateTime <- get[LocalDateTime]("dateTime")
  } yield Notification(id, idRecipient, idRequester, typeNotif, NotificationStatus(statusNotif.toInt), dateTime)


  override def table: String = "notification"

  override def getParams(item: Notification): Seq[NamedParameter] = Seq[NamedParameter](
    'idRecipient -> item.idRecipient,
    'idRequester -> item.idRequester,
    'idType -> item.typeNotif,
    'idStatus -> item.status.id.toLong,
    'dateTime -> item.dateTime
  )


  def findNotificationByDateAndRecipient(idRecipient: String, dateTime: LocalDateTime)(implicit c: Connection): Seq[Notification] = {
    SQL(
      s"""
         SELECT * FROM NOTIFICATION WHERE idRecipient = {idRecipient} and dateTime > {dt}
       """).on("idRecipient" -> idRecipient, "dt" -> dateTime).as(rowParser.*)
  }
}
