package services


import config.Settings
import controllers.jsonUtils
import dao.{AdminAccountDAO, SponsorDAO}
import model.Account
import pdi.jwt.{Jwt, JwtAlgorithm}
import play.api.db.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by fsznajderman on 28/02/2017.
  */
trait AuthService {


  def validAuthentification(login: String, password: String, f: String => Option[Account]): Future[User]

  def validReportAuthentification(login: String, password: String): Future[User]

  def validJwToken(token: String): Future[User]

  def isAdmin(login: String): Option[Account]

  def isRepresentative(regId: String): Boolean


}


class AuthServiceImpl(db: Database, remote: RemoteClient) extends AuthService {


  override def validReportAuthentification(login: String, password: String): Future[User] = {


    auth(login, password).map {
      case a: UnauthenticateUser => a
      case user@AuthenticateUser(_, _, _, token) => {
        db.withConnection(implicit connection => {
          val regid = jsonUtils.regIdExtractor(token)
          SponsorDAO.isRepresentative(regid) match {
            case None => UnauthenticateUser(s"User for the following RegId ${regid} is not an representative")
            case Some(_) => user.copy(token = {
              Jwt.encode(s"""{"registrantId":"${regid}", "email":"${user.email}"}""", Settings.oAuth.localSecret, JwtAlgorithm.HS256)
            })
          }
        })
      }
    }
  }


  override def validAuthentification(login: String, password: String, f: String => Option[Account]): Future[User] = {
    val admin = f(login)
    // TODO gérer le cas où le client retourne un 401 Unauthorized
    admin match {
      case None => Future.successful(UnauthenticateUser("None admin user has been found for this email"))
      case Some(_) => auth(login, password)

    }
  }

  private def auth(login: String, password: String): Future[User] = {
    for {
      jeton <- remote.getJWtToken(login, password)
      user <- remote.getUserInfo(jeton)
    } yield user
  }


  override def validJwToken(token: String): Future[User] = {
    remote.getUserInfo(token)
  }


  def checkAdmin(token: String): Option[Account] = {

    val email = jsonUtils.emailExtractor(token)
    isAdmin(email)

  }


  def isAdmin(login: String): Option[Account] = {
    db.withConnection { implicit connection =>
      AdminAccountDAO.findBy("email_adress", login)

    }

  }

  def isRepresentative(regid: String): Boolean = {

    db.withConnection { implicit connection =>
      SponsorDAO.isRepresentative(regid)
    } match {
      case Some(_) => true
      case None => false
    }
  }


}







