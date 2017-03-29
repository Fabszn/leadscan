package model

import java.time.LocalDateTime

import model.NotificationStatus.NotifStatus

/**
  * Created by fsznajderman on 19/01/2017.
  */
case class Notification(id: Option[Long], idRecipient: String, idRequester: String, typeNotif: Long, status: NotifStatus, dateTime: LocalDateTime)


object NotificationStatus extends Enumeration {

  type NotifStatus = Value
  val UNREAD = Value(1, "Read")
  val READ = Value(2, "UnRead")


}


object NotificationType extends Enumeration {
  type NotifType = Value
  val  Connected = Value(1,"Connected")
  val  requestForUpdate = Value(2,"request_for_update")
}