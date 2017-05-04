package services

import config.Settings
import model.{Event, ImportRegistration, PersonJson}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import utils.LoggerAudit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by fsznajderman on 01/03/2017.
  */

sealed trait User

case class UnauthenticateUser(msg: String) extends User

case class AuthenticateUser(firstname: String, lastname: String, email: String, token: String) extends User


case class MyDevoxxPerson(userID: String,
                          registrantId: Option[String],
                          firstName: String,
                          lastName: String,
                          gender: Option[String],
                          company: String,
                          job: String,
                          address1: Option[String],
                          address2: Option[String],
                          region: Option[String],
                          city: Option[String],
                          zip: Option[String],
                          country: Option[String],
                          phone: Option[String]
                         )

trait RemoteClient {

  def getJWtToken(login: String, password: String, remenberMe: Boolean = false): Future[String]

  def getUserInfo(): Future[User]

  def getUserInfo(token: String): Future[User]

  def sendPerson(person: PersonJson): Future[String]

  def sendPassword(regId: String, pass: String): Future[String]

  def loadByregId(regId: String): Future[MyDevoxxPerson]

}


class MyDevoxxRemoteClient(ws: WSClient, es: EventService) extends RemoteClient with LoggerAudit {

  // Todo Je retournerai un Future[Try[String]] pour gérer si authentification a échoué
  override def getJWtToken(login: String, password: String, remenberMe: Boolean = false): Future[String] = {

    ws.url(Settings.oAuth.endpoints.auth)
      .withHeaders("Content-Type" -> "application/json")
      .post(Json.obj("email" -> login, "password" -> password, "rememberMe" -> remenberMe))
      .map(wsRes => (wsRes.json \ "token").as[String])
      .recover {
        case e: Exception =>
          play.Logger.error("Unable to load JWT Token due to ", e)
          s"${e.getMessage} - ${e.getCause}"
      }


  }


  override def getUserInfo(token: String): Future[User] = {

    logger.info("getUserInfo")
    val r = for {
      json <- ws.url(Settings.oAuth.endpoints.userinfo)
        .withHeaders("Content-Type" -> "application/json", "Accept" -> "application/json", "X-Auth-Token" -> token).get().map(w => w.json)
    } yield {
      val firstName = (json \ "firstName").as[String]
      val lastName = (json \ "lastName").as[String]
      val email = (json \ "email").as[String]
      val userId = (json \ "userID").as[String]

      AuthenticateUser(firstName, lastName, email, token)
    }

    r.recover {
      case e: Throwable => UnauthenticateUser(e.getMessage)
    }

    r
  }


  override def getUserInfo: Future[User] = {

    logger.info("getUserInfo")
    val r = for {
      token <- getJWtToken(Settings.oAuth.myDevoxxLogin, Settings.oAuth.myDevoxxpwd)
      json <- ws.url(Settings.oAuth.endpoints.userinfo)
        .withHeaders("Content-Type" -> "application/json", "Accept" -> "application/json", "X-Auth-Token" -> token).get().map(w => w.json)
    } yield {
      val firstName = (json \ "firstName").as[String]
      val lastName = (json \ "lastName").as[String]
      val email = (json \ "email").as[String]
      val userId = (json \ "userID").as[String]

      AuthenticateUser(firstName, lastName, email, token)
    }

    r.recover {
      case e: Throwable => UnauthenticateUser(e.getMessage)
    }

    r
  }

  override def sendPerson(p: PersonJson): Future[String] = {

    logger.info("Person")
    val personToSend = Json.toJson(Json.obj(
      "email" -> p.email,
      "registrantId" -> p.regId,
      "firstName" -> p.firstname,
      "lastName" -> p.lastname,
      "company" -> p.company,
      "job" -> p.title,
      "city" -> p.city,
      "phone" -> p.phone))

    logger.debug(s"Person sent to Mydevoxx $personToSend")

    val r = for {
      token <- getJWtToken(Settings.oAuth.myDevoxxLogin, Settings.oAuth.myDevoxxpwd)
      response <- ws.url(Settings.oAuth.endpoints.createPerson).withHeaders("Content-Type" -> "application/json", Settings.oAuth.TOKEN_KEY -> token).post(personToSend).map(r => r.body)
    } yield response
    r.onComplete(r => {
      es.addEvent(Event(typeEvent = ImportRegistration.typeEvent, message = s" $r as answer for ${
        personToSend.toString
      } "))
    })
    r
  }

  override def sendPassword(regId: String, pass: String): Future[String] = {


    logger.info("sendPassword")
    val body = Json.toJson(Json.obj("password" -> pass))

    val r = for {
      token <- getJWtToken(Settings.oAuth.myDevoxxLogin, Settings.oAuth.myDevoxxpwd)
      response <- ws.url(s"${
        Settings.oAuth.endpoints.createPassword
      }${
        regId
      }").withHeaders("Content-Type" -> "application/json", Settings.oAuth.TOKEN_KEY -> token).post(body).map(r => r.body)
    } yield response


    r.onComplete(r => logger.debug(s" ${
      r.toString
    } response from myDevoxx for password $r"))
    r

  }

  override def loadByregId(regId: String): Future[MyDevoxxPerson] = {

    logger.info(s" URI ${
      Settings.oAuth.endpoints.personByRegId
    }${
      regId
    }")


    for {
      token <- getJWtToken(Settings.oAuth.myDevoxxLogin, Settings.oAuth.myDevoxxpwd)
      json <- ws.url(s"${
        Settings.oAuth.endpoints.personByRegId
      }${
        regId
      }").withHeaders("Content-Type" -> "application/json", Settings.oAuth.TOKEN_KEY -> token).get().map(r => r.body)
    } yield {
      implicit val myDevoxxPerson = Json.reads[MyDevoxxPerson]
      logger.info(s" json $json")
      Json.parse(json).as[MyDevoxxPerson]
    }
  }


}


