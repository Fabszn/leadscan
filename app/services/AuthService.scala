package services


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by fsznajderman on 28/02/2017.
  */
trait AuthService {


  def validAuthentifiaction(login: String, password: String): Future[User]

  def validJwToken(token: String): Future[User]

}


class AuthServiceImpl(remote: RemoteClient) extends AuthService {


  override def validAuthentifiaction(login: String, password: String): Future[User] = {


    for {
      jeton <- remote.getJWtToken(login, password)
      user <- remote.getUserInfo(jeton)
    } yield user
  }

  override def validJwToken(token: String): Future[User] = {
    remote.getUserInfo(token)
  }

}




