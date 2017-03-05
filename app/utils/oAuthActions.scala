package utils

import java.time.{Instant, LocalDateTime}

import config.Settings
import play.api.mvc.{ActionBuilder, Request, Result, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by fsznajderman on 01/03/2017.
  */
object oAuthActions extends LoggerAudit {


  object ApiAuthAction extends ActionBuilder[Request] with Results {

    val TOKEN_KEY = "X-Auth-Token"

    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
      request.headers.get("Authorization").map(_.toLong) match {
        case Some(exp) if exp > Instant.now.getEpochSecond => block(request)
        case Some(exp) => logger.info(s"Token expired (now: ${Instant.now.getEpochSecond} / exp: $exp)")
          Future.successful(Unauthorized(""))
        case None => Future.successful(Unauthorized(""))
      }
    }
  }


  object AdminAuthAction extends ActionBuilder[Request] with Results {


    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
      logger.info(s"SecurityCheck - Admin [${request.path}]")

      request.session.get("exp") match {
        case None => Future.successful(Unauthorized("Not Authorised - no session found"))
        case Some(v) if checkExpiration(LocalDateTime.parse(v)) => Future.successful(Unauthorized("Not Authorised - session has been expired").withNewSession)

        case Some(_) => {
          //update expiration date
          block(request).map(r => r.withSession(request.session.+("exp", LocalDateTime.now().plusMinutes(Settings.session.timeout_mn).toString)))
        }
      }
    }
  }

  private def checkExpiration(expDateTime: LocalDateTime): Boolean = {
    logger.info(s"check date " + expDateTime)

    val now = LocalDateTime.now()
    now.isAfter(expDateTime)
  }


}
