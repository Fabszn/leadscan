package services


import dao.AdminAccountDAO
import play.api.db.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by fsznajderman on 28/02/2017.
  */
trait AuthService {


  def validAuthentifiaction(login: String, password: String): Future[User]

  def validJwToken(token: String): Future[User]

}


class AuthServiceImpl(db: Database, remote: RemoteClient) extends AuthService {


  override def validAuthentifiaction(login: String, password: String): Future[User] = {
    val admin = db.withConnection { implicit connection =>
      AdminAccountDAO.findBy("email_adress", login)
    }

    admin match {
      case None => Future.successful(UnauthenticateUser("None admin user has been found for this email"))
      case Some(_) => for {
        jeton <- remote.getJWtToken(login, password)
        user <- remote.getUserInfo(jeton)
      } yield user
    }
  }


  override def validJwToken(token: String): Future[User] = {
    remote.getUserInfo(token)
  }

}




