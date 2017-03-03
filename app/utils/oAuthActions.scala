package utils

import java.time.Instant

import config.Settings
import play.api.mvc.{ActionBuilder, Request, Result, Results}

import scala.concurrent.Future

/**
  * Created by fsznajderman on 01/03/2017.
  */
object oAuthActions {



  val TOKEN_KEY = "X-Auth-Token"

  object ApiAuthAction extends ActionBuilder[Request] with Results with LoggerAudit {


    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
      request.headers.get("Authorization").map(_.toLong) match {
        case Some(exp) if exp > Instant.now.getEpochSecond => block(request)
        case Some(exp) => logger.info(s"Token expired (now: ${Instant.now.getEpochSecond} / exp: $exp)")
          Future.successful(Unauthorized(""))
        case None => Future.successful(Unauthorized(""))
      }
    }
  }


  object AdminAuthAction extends ActionBuilder[Request] with Results with LoggerAudit {


    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
      logger.info(s"SecurityCheck - Admin [${request.path}]")

      request.headers.get(TOKEN_KEY) match {
        case None => Future.successful(Unauthorized("Not Authorised"))
        case Some(v) if v.isEmpty => Future.successful(Unauthorized("KO"))
        case Some(v)  => block(request)
      }


    }
  }

}
