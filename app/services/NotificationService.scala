package services

import java.time.LocalDateTime

import dao.NotificationDAO
import model.{Notification, Person}
import play.api.db.Database
import play.api.libs.mailer.{Email, MailerClient}


/**
  * Created by fsznajderman on 29/01/2017.
  */
trait NotificationService {

  def addNotification(notif: Notification): Unit

  def getNotifications(idRecipient: Long, dateTime: LocalDateTime): Seq[Notification]

  def getNotification(idNotification: Long): Option[Notification]

  def sendMail(p: Person, dest: Seq[String], pass: String): Unit

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


  override def sendMail(p: Person, dest: Seq[String], pass: String): Unit = {


    mailer.send(Email("Your password", "program@devoxx.us", dest, None, Some(
      s"""
       Dear ${p.firstname}<br>
         |<br>
         |You're recei ving this email because someone promoted your profile as a representative for company @company.<br>
         |
          |You can now connect to the Leadscan application and start to scan attendees.<br>
         |Your default password is : ${pass} <br>
         |
 |The Scan application works on Android, iPhone and in a web browser. Please open a web browser to http://mydevoxx-pwa.cleverapps.io/ <br><br>
         |
 |If you want to change this password, please visit https://my-devoxx-us.cleverapps.io <br><br>
         |
 |Feel free to contact the organization team if you need more help <br><br>
         |
 |Kind regards<br><br>
         |
 |The Devoxx US Team<br><br>

      """.stripMargin)))

  }

}
