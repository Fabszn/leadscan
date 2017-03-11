package services

import java.time.LocalDateTime

import config.Settings
import dao.NotificationDAO
import model.Notification
import play.api.db.Database
import play.api.libs.mailer.{Email, MailerClient}


/**
  * Created by fsznajderman on 29/01/2017.
  */
trait NotificationService {

  def addNotification(notif: Notification): Unit

  def getNotifications(idRecipient: Long, dateTime: LocalDateTime): Seq[Notification]

  def getNotification(idNotification: Long): Option[Notification]

  def sendMail(dest: Seq[String], body: String): Unit
}


class NotificationServiceImpl(db: Database, mailer: MailerClient, remote: RemoteClient) extends NotificationService {

  override def addNotification(notif: Notification): Unit = {

    db.withConnection(implicit c =>
      NotificationDAO.create(notif)
    )
  }

  override def getNotifications(idRecipient: Long, dateTime: LocalDateTime): Seq[Notification] = {

    db.withConnection(implicit c =>
      NotificationDAO.findNotificationByDateAndRecipient(idRecipient, dateTime)
    )
  }

  override def getNotification(idNotification: Long): Option[Notification] = {
    db.withConnection(implicit c =>
      NotificationDAO.find(idNotification)

    )
  }


  override def sendMail(dest: Seq[String], body: String): Unit =
    mailer.send(Email("Your password", Settings.play.mailer.from, dest, None, Some(body), bcc = Seq(Settings.play.mailer.bcc)))


}

