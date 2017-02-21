package config

import com.typesafe.config.ConfigFactory

/**
  * Created by fsznajderman on 03/02/2017.
  */
object Settings {

  private val config = ConfigFactory.load


  val headers = config.getString("export.headers")

}
