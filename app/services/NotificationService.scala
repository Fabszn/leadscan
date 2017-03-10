package services

import java.time.LocalDateTime

import dao.NotificationDAO
import model.Notification
import play.api.db.Database
import play.api.libs.mailer.{Email, MailerClient}
import utils.PasswordGenerator


/**
  * Created by fsznajderman on 29/01/2017.
  */
trait NotificationService {

  def addNotification(notif: Notification): Unit

  def getNotifications(idRecipient: Long, dateTime: LocalDateTime): Seq[Notification]

  def getNotification(idNotification: Long): Option[Notification]

  def sendMail(dest: Seq[String]): Unit

}


class NotificationServiceImple(db: Database, mailer: MailerClient) extends NotificationService {

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


  override def sendMail(dest: Seq[String]): Unit = {

val mdpTemp= PasswordGenerator.generatePassword
    mailer.send(Email("Your password", "program@devoxx.us", dest, Some(
      s"""
         |Hello
         |
        |You are now representative for the following sponsor :
         |
        |below your password to connect you to the lead scan web application application : http://....
         |
        |
        |your temporary password is : ${mdpTemp}
         |

      """.stripMargin), Some(
      s"""
         |Hello
         |
        |You are now representative for the following sponsor :
         |
        |below your password to connect you to the lead scan web application application : http://....
         |
        |
        |your temporary password is : ${mdpTemp}
         |

      """.stripMargin)))

  }
}
