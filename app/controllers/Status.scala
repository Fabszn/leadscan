package controllers

import buildinfo.BuildInfo
import play.api.mvc.{Action, Controller}
import utils.CORSAction

/**
  * Created by fsznajderman on 16/01/2017.
  */
class Status extends Controller {

  def status = CORSAction {
    Ok(BuildInfo.toJson)
  }

  def index = CORSAction {
    Ok(views.html.login())
  }

}
