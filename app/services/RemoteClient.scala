package services

import model.PersonJson
import play.api.libs.json.Json

import scala.concurrent.Future


sealed trait User

case class UnauthenticateUser(msg: String) extends User

case class AuthenticateUser(firstname: String, lastname: String, email: String, token: String) extends User

object AuthenticateUser {
  implicit val format = Json.format[AuthenticateUser]
}

trait RemoteClient {

  def getJWtToken(login: String, password: String, remenberMe: Boolean = false): Future[String]

  def getUserInfo(): Future[User]

  def getUserInfo(token: String): Future[User]

  def sendPerson(person: PersonJson): Future[String]

  def sendPassword(regId: String, pass: String): Future[String]

  def loadByregId(regId: String): Future[MyDevoxxPerson]

}