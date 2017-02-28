package controllers

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, __}
import play.api.mvc.{Action, Controller}

/**
  * Created by fsznajderman on 28/02/2017.
  */
class SecurityController extends Controller {


  case class UserData(login: String, pass: String)

  def check = Action(parse.json) { implicit request =>

    implicit val readUserData: Reads[UserData] = (
      (__ \ "login").read[String] and (__ \ "password").read[String]
      ) (UserData.apply _)


    request.body.validate[UserData].asEither match{
      case Left(e) => InternalServerError(e.toString())
      case Right(userData) => Found(controllers.routes.AdminController.home.url)
    }



  }

}
