package services

import config.Settings
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by fsznajderman on 01/03/2017.
  */


sealed trait User

case class UnauthenticateUser(msg: String) extends User

case class AuthenticateUser(firstname: String, lastname: String, email: String, userId: String) extends User


trait RemoteClient {
  def getJWtToken(login: String, password: String, remenberMe: Boolean = false): Future[String]

  def getUserInfo(token: String): Future[User]

}


class MyDevoxxRemoteClient(ws: WSClient) extends RemoteClient {


  def getJWtToken(login: String, password: String, remenberMe: Boolean): Future[String] = {

    ws.url(Settings.oAuth.endpoints.auth)
      .withHeaders("Content-Type" -> "application/json")
      .post(Json.obj("email" -> login, "password" -> password, "rememberMe" -> remenberMe)).map(wsRes => (wsRes.json \ "token").as[String]).recover { case e: Exception => s"${e.getMessage} - ${e.getCause}" }


  }

  def getUserInfo(token: String): Future[User] =
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


