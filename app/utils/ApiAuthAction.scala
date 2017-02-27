package utils

import java.time.Instant

import config.Settings
import play.api.mvc.{ActionBuilder, Request, Result, Results}

import scala.concurrent.Future

/**
  * Created by fsznajderman on 27/02/2017.
  */
object AuthAction extends ActionBuilder[Request] with Results with LoggerAudit{



  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    request.session.get("exp").map(_.toLong) match {
      case Some(exp) if exp > Instant.now.getEpochSecond => block(request)
      case Some(exp) => logger.info(s"Token expired (now: ${Instant.now.getEpochSecond} / exp: $exp)")
        Future.successful(Found(Settings.oAuth.authorizationEndpoint))
      case None => Future.successful(Found(Settings.oAuth.authorizationEndpoint))
    }
  }
}

