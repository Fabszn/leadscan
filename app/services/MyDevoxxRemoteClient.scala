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


trait RemoteClient {
  def getJWtToken(login: String, password: String, remenberMe: Boolean = false): Future[String]

  def getUserInfo(token: String): Future[User]

  def sendPerson(person: PersonJson, token: String): Future[String]

  def sendPassword(regId: String, pass: String, token: String): Future[String]

}


class MyDevoxxRemoteClient(ws: WSClient, es: EventService) extends RemoteClient with LoggerAudit {

  // Todo Je retournerai un Future[Try[String]] pour gérer si authentification a échoué
  override def getJWtToken(login: String, password: String, remenberMe: Boolean): Future[String] = {

    ws.url(Settings.oAuth.endpoints.auth)
      .withHeaders("Content-Type" -> "application/json")
      .post(Json.obj("email" -> login, "password" -> password, "rememberMe" -> remenberMe))
      .map(wsRes => (wsRes.json \ "token").as[String])
      .recover {
        case e: Exception =>
          play.Logger.error("Unable to load JWT Token due to ",e)
          s"${e.getMessage} - ${e.getCause}"
      }


  }

  override def getUserInfo(token: String): Future[User] = {

    logger.info("getUserInfo")
    ws.url(Settings.oAuth.endpoints.userinfo)
      .withHeaders("Content-Type" -> "application/json", "Accept" -> "application/json", "X-Auth-Token" -> token).get().map { wsR => {
      val firstName = (wsR.json \ "firstName").as[String]
      val lastName = (wsR.json \ "lastName").as[String]
      val email = (wsR.json \ "email").as[String]
      val userId = (wsR.json \ "userID").as[String]


      AuthenticateUser(firstName, lastName, email, token)
    }
    }.recover {
      case e: Throwable => UnauthenticateUser(e.getMessage)
    }

  }

  override def sendPerson(p: PersonJson, token: String): Future[String] = {

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

    val response = ws.url(Settings.oAuth.endpoints.createPerson).withHeaders("Content-Type" -> "application/json", Settings.oAuth.TOKEN_KEY -> token).post(personToSend).map(r => r.body)
    response.onComplete(r => {
      es.addEvent(Event(typeEvent=ImportRegistration.typeEvent,message=s" $r as answer for ${personToSend.toString} "))
    })
    response
  }

  override def sendPassword(regId: String, pass: String, token: String): Future[String] = {

    logger.info("sendPassword")
    val body = Json.toJson(Json.obj("password" -> pass))

    val response = ws.url(s"${Settings.oAuth.endpoints.createPassword}${regId}").withHeaders("Content-Type" -> "application/json", Settings.oAuth.TOKEN_KEY -> token).post(body).map(r => r.body)
    response.onComplete(r => logger.debug(s" ${r.toString} response from myDevoxx for password $r"))
    response

  }
}


