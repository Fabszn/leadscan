package controllers

import play.api.db.Database
import play.api.mvc.{Action, Controller}

/**
  * Created by fsznajderman on 11/01/2017.
  */
class Index(db : Database) extends Controller{

  def index() = Action{
    Ok("Hello World")
  }

}
