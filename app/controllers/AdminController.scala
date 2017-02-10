package controllers

import play.api.mvc.{Action, Controller}
import utils.LoggerAudit

/**
  * Created by fsznajderman on 10/02/2017.
  */
class AdminController extends Controller with LoggerAudit {


  def index = Action {
    Ok(views.html.index())
  }

}
