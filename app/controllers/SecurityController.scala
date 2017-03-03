package controllers

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Json, Reads, __}
import play.api.mvc.{Action, Controller}
import services.{AuthService, AuthenticateUser, UnauthenticateUser}
import utils.{LoggerAudit, oAuthActions}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by fsznajderman on 28/02/2017.
  */
class SecurityController(authService: AuthService) extends Controller with LoggerAudit {


  case class UserData(login: String, pass: String)

  def check = Action.async(parse.json) { implicit request =>


    implicit val readUserData: Reads[UserData] = (
      (__ \ "login").read[String] and (__ \ "password").read[String]
      ) (UserData.apply _)


    request.body.validate[UserData].asEither match {
      case Left(e) => Future.successful(Unauthorized(e.toString()))
      case Right(userData) => {

        authService.validAuthentifiaction(userData.login, userData.pass).map {
          case UnauthenticateUser(error) => {
            logger.info(s"User : ${userData.login} is no authorised - [$error]")
            Unauthorized(s"User : ${userData.login} is no authorised - [$error]")
          }
          case AuthenticateUser(_, _, email, jwt) =>
            Ok(Json.toJson(Map(oAuthActions.TOKEN_KEY -> jwt, "mail" -> {
              s"$email"
            })))

        }
      }
    }
  }
}
