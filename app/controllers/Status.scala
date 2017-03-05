package controllers

import buildinfo.BuildInfo
import play.api.mvc.Controller
import utils.CORSAction

/**
  * Created by fsznajderman on 16/01/2017.
  */
class Status extends Controller {

  def status = CORSAction {
    Ok(BuildInfo.toJson)
  }





}
