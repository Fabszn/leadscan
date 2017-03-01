package controllers

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Json, Reads, __}
import play.api.mvc.{Action, Controller}
import services.{AuthService, UserAuthorisation, UserNoAuthrisation}
import utils.oAuthActions

/**
  * Created by fsznajderman on 28/02/2017.
  */
class SecurityController(authService: AuthService) extends Controller {





  case class UserData(login: String, pass: String)

  def check = Action(parse.json) { implicit request =>

    implicit val readUserData: Reads[UserData] = (
      (__ \ "login").read[String] and (__ \ "password").read[String]
      ) (UserData.apply _)


    request.body.validate[UserData].asEither match {
      case Left(e) => Unauthorized(e.toString())
      case Right(userData) => {
        //check from Mydevoxx service auth
        authService.validAuthentifiaction(userData.login, userData.pass) match {
          case UserNoAuthrisation => Unauthorized(s"User : ${userData.login} is no authorised")
          case UserAuthorisation(regid, mail, jwt) =>
            Ok(Json.toJson(Map(oAuthActions.TOKEN_KEY -> jwt, "mail" -> mail)))

        }
      }


    }

  }


}
