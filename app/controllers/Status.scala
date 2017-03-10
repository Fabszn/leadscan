package controllers

import buildinfo.BuildInfo
import play.api.mvc.{Action, Controller}
import services.NotificationService

/**
  * Created by fsznajderman on 16/01/2017.
  */
class Status(notif: NotificationService) extends Controller {

  def status = Action {
    Ok(BuildInfo.toJson)
  }





}
