package utils

import java.time.LocalDateTime

import config.Settings
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by fsznajderman on 01/03/2017.
  */
object oAuthActions extends LoggerAudit {


  object  ApiAuthAction extends ActionBuilder[Request] with Results {

    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
      block(request)
      /*request.session.get("connected") match {
        case Some(_) => block(request)
        case None => Future.successful(Unauthorized("Not Authorised - no session found"))
      }*/
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
