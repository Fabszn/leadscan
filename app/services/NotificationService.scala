package services

import java.time.LocalDateTime

import config.Settings
import dao.NotificationDAO
import model.Notification
import play.api.db.Database
import play.api.libs.mailer.{Email, MailerClient}
import utils.LoggerAudit


/**
  * Created by fsznajderman on 29/01/2017.
  */
trait NotificationService {

  def addNotification(notif: Notification): Unit

  def getNotifications(idRecipient: Long, dateTime: LocalDateTime): Seq[Notification]

  def getNotification(idNotification: Long): Option[Notification]

  def sendMail(dest: Seq[String], bodyText: Option[String], bodyHtml: Option[String]): Unit
}


class NotificationServiceImpl(db: Database, mailer: MailerClient, remote: RemoteClient) extends NotificationService with LoggerAudit {

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


  override def sendMail(dest: Seq[String], bodyText: Option[String], bodyHtml: Option[String]): Unit = {
    logger.info(s"From ${Settings.play.mailer.from}")
    logger.info(s"Bcc ${Settings.play.mailer.bcc}")
    logger.info(s"dest ${dest.mkString(",")}")

    mailer.send(Email("Devoxx US Sponsor - your Lead Generation System credentials", Settings.play.mailer.from, dest, bodyText, bodyHtml, bcc = Seq(Settings.play.mailer.bcc)))

  }
}

