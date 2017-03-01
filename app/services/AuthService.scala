package services

/**
  * Created by fsznajderman on 28/02/2017.
  */
trait AuthService {

  type JwtToken = String
  type Error = String

  def validAuthentifiaction(login: String, password: String): UserAuth

}


sealed trait UserAuth

case object UserNoAuthrisation extends UserAuth

case class UserAuthorisation(regId: String, mail: String, jwtToken: String) extends UserAuth


class AuthServiceImpl extends AuthService {
  override def validAuthentifiaction(login: String, password: String): UserAuth = ???
}


class AuthServiceMockImpl extends AuthService {
  override def validAuthentifiaction(login: String, password: String): UserAuth = {
    if (login == "error") {
      UserNoAuthrisation
    } else {
     UserAuthorisation("1234", "louis@gmail.com", "123456")
    }
  }
}

