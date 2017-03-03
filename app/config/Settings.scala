package config

import com.typesafe.config.ConfigFactory

/**
  * Created by fsznajderman on 03/02/2017.
  */
object Settings {

  private val config = ConfigFactory.load


  val headersSponsor = config.getString("export.headers.sponsor")
  val headersEvent = config.getString("export.headers.events")

  object oAuth {


    private val urlbase = config.getString("oAuth.url.base")

    object endpoints {

      val auth = urlbase + config.getString("oAuth.endpoints.auth")
      val userinfo = urlbase + config.getString("oAuth.endpoints.userinfo")
    }

  }


}

