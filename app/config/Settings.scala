package config

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions
import scala.reflect.internal.util.ScalaClassLoader

/**
 * Created by fsznajderman on 03/02/2017.
 */
object Settings {

  private val config = ConfigFactory.load

  val listSponsorsUrl: String = config.getString("list.sponsors.url")

  val headersSponsor: String = config.getString("export.headers.sponsor")
  val headersRepresentative: String = config.getString("export.headers.representative")
  val headersEvent: String = config.getString("export.headers.events")

  object oAuth {


    val TOKEN_KEY = "X-Auth-Token"

    private val urlbase = config.getString("oAuth.url.base")

    val sharedSecret = config.getString("oAuth.jwt.sharedsecret")
    val localSecret = config.getString("oAuth.jwt.localsecret")
    val myDevoxxLogin = config.getString("oAuth.creds.login")
    val myDevoxxpwd = config.getString("oAuth.creds.pwd")

    object endpoints {
      val auth: String = urlbase + config.getString("oAuth.endpoints.auth")
      val userinfo: String = urlbase + config.getString("oAuth.endpoints.userinfo")
      val createPerson: String = urlbase + config.getString("oAuth.endpoints.createPerson")
      val createPassword: String = urlbase + config.getString("oAuth.endpoints.password")
      val personByRegId: String = urlbase + config.getString("oAuth.endpoints.personByRegId")
    }


  }

  object play {


    object mailer {
      val from: String = config.getString("play.mailer.from")
      val bcc: String = config.getString("play.mailer.bcc")
      val committee: String = config.getString("play.mailer.committee")
    }

  }

  object tls {

    object enable {

      val https: Boolean = config.getBoolean("tls.enable.https")
    }

  }

  object session {

    val timeout_mn: Long = config.getLong("session.timeout.mn")

  }


}

