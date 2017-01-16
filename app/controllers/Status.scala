package controllers

import buildinfo.BuildInfo
import play.api.mvc.{Action, Controller}

/**
  * Created by fsznajderman on 16/01/2017.
  */
class Status extends Controller {

  def status = Action {
    Ok(BuildInfo.toJson)
  }

}
