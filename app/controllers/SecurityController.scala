package controllers

import java.time.LocalDateTime

import config.Settings
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Json, Reads, __}
import play.api.mvc.{Action, Controller}
import services.{AuthService, AuthenticateUser, UnauthenticateUser}
import utils.LoggerAudit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by fsznajderman on 28/02/2017.
  */
class SecurityController(authService: AuthService) extends Controller with LoggerAudit {


  case class UserData(login: String, pass: String)

  def signout = Action.async { implicit request =>
    logger.info("disconnected")
    Future.successful(Ok("disconnected").withNewSession)
  }

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
          case AuthenticateUser(_, _, email, _) => {
            logger.info(s"$userData authenticated")
            Ok(Json.toJson(Map("mail" -> email))).withSession("connected" -> s"$email", "exp" -> LocalDateTime.now().plusMinutes(Settings.session.timeout_mn).toString)
          }
          case _ => {
            logger.info(s"Strange behavior :(")
            Unauthorized("Strange behavior :(")
          }

        }
      }
    }
  }
}
