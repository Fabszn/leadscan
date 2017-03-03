package services


import scala.concurrent.Future


/**
  * Created by fsznajderman on 28/02/2017.
  */
trait AuthService {


  def validAuthentifiaction(login: String, password: String): Future[User]

}


class AuthServiceImpl(remote: RemoteClient) extends AuthService {


  override def validAuthentifiaction(login: String, password: String): Future[User] = {

    import scala.concurrent.ExecutionContext.Implicits.global

    for {
      jeton <- remote.getJWtToken(login, password)
      user <- remote.getUserInfo(jeton)
    } yield user
  }

}




