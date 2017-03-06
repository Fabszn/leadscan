package config

import com.typesafe.config.ConfigFactory

/**
  * Created by fsznajderman on 03/02/2017.
  */
object Settings {

  private val config = ConfigFactory.load


  val headersSponsor: String = config.getString("export.headers.sponsor")
  val headersEvent: String = config.getString("export.headers.events")

  object oAuth {


    val TOKEN_KEY = "X-Auth-Token"

    private val urlbase = config.getString("oAuth.url.base")

    object endpoints {
      val auth: String = urlbase + config.getString("oAuth.endpoints.auth")
      val userinfo: String = urlbase + config.getString("oAuth.endpoints.userinfo")
    }



  }

  object session {

    val timeout_mn: Long = config.getLong("session.timeout.mn")

  }

}

