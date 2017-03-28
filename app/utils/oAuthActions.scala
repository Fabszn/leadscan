package utils

import java.time.LocalDateTime

import config.Settings
import pdi.jwt.{Jwt, JwtAlgorithm}
import play.api.mvc._
import utils.oAuthActions.AdminAuthAction.logger
import utils.oAuthActions.ReportsAuthAction.logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Created by fsznajderman on 01/03/2017.
  */
object oAuthActions extends LoggerAudit {


  object ApiAuthAction extends ActionBuilder[Request] with Results with LoggerAudit {

    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
      logger.info("info API =>" + request.uri)

      if (Settings.tls.enable.https && request.headers.get("X-Forwarded-Proto").getOrElse("http") != "https") {
        logger.info("API - ssl redirect")
        Future.successful(Results.MovedPermanently("https://" + request.host + request.uri))
      } else {
        request.headers.get(Settings.oAuth.TOKEN_KEY) match {
          case Some(token) =>
            Try {
              Jwt.validate(token, Settings.oAuth.sharedSecret, Seq(JwtAlgorithm.HS256))
            } match {
              case Success(_) => block(request)
              case Failure(_) => Future.successful(Unauthorized("Not Authorised - token is invalid"))
            }
          case None => Future.successful(Unauthorized("Not Authorised - token not found"))
        }
      }
    }


  }

  object AdminRootAction extends ActionBuilder[Request] with Results with LoggerAudit {

    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {

      logger.info("info ADMIN Root =>" + request.uri)

      if (Settings.tls.enable.https && request.headers.get("X-Forwarded-Proto").getOrElse("http") != "https") {
        logger.info("Admin - ssl redirect")
        Future.successful(Results.MovedPermanently("https://" + request.host + request.uri))
      } else {
        block(request)
      }
    }


  }


  object AdminAuthAction extends ActionBuilder[Request] with Results with LoggerAudit {


    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {

      logger.info("info ADMIN =>" + request.uri)


      logger.info("Admin -  ssl redirect")
      if (Settings.tls.enable.https && request.headers.get("X-Forwarded-Proto").getOrElse("http") != "https") {
        Future.successful(Results.MovedPermanently("https://" + request.host + request.uri))
      } else {
        request.session.get("token") match {
          case Some(token) =>
            logger.info(s"ADMIN token ${token}")

            Try {
              Jwt.validate(token, Settings.oAuth.sharedSecret, Seq(JwtAlgorithm.HS256))
            } match {

              case Success(_) => request.session.get("exp") match {
                case None => Future.successful(Unauthorized("Not Authorised - no session found"))
                case Some(v) if checkExpiration(LocalDateTime.parse(v)) => Future.successful(Unauthorized("Not Authorised - session has been expired").withNewSession)
                case Some(_) =>
                  //update expiration date
                  block(request).map(r => r.withSession(request.session.+("exp", LocalDateTime.now().plusMinutes(Settings.session.timeout_mn).toString)))
              }
              case Failure(_) => Future.successful(Unauthorized("Not Authorised - token is invalid"))
            }
          case None => {


            Future.successful(Unauthorized("Not Authorised - token not found"))
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


  object ReportsAuthAction extends ActionBuilder[Request] with Results with LoggerAudit {


    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {

      logger.info("info REPORTS =>" + request.uri)
      //TODO ajouter un check sur le fait que la personne est bien representative

      logger.info("REPORTS -  ssl redirect")
      if (Settings.tls.enable.https && request.headers.get("X-Forwarded-Proto").getOrElse("http") != "https") {
        Future.successful(Results.MovedPermanently("https://" + request.host + request.uri))
      } else {
        request.session.get("token") match {
          case Some(token) =>
            logger.info(s"REPORTS token ${token}")
            Try {
              Jwt.validate(token, Settings.oAuth.localSecret, Seq(JwtAlgorithm.HS256))
            } match {
              case Success(_) => request.session.get("exp") match {
                case None => Future.successful(Unauthorized("Not Authorised - no session found"))
                case Some(v) if checkExpiration(LocalDateTime.parse(v)) => Future.successful(Unauthorized("Not Authorised - session has been expired").withNewSession)
                case Some(_) =>
                  //update expiration date
                  block(request).map(r => r.withSession(request.session.+("exp", LocalDateTime.now().plusMinutes(Settings.session.timeout_mn).toString)))
              }
              case Failure(_) => Future.successful(Unauthorized("Not Authorised - token is invalid"))
            }
          case None => Future.successful(Unauthorized("Not Authorised - token not found"))
        }
      }
    }

    private def checkExpiration(expDateTime: LocalDateTime): Boolean = {
      logger.info(s"check date " + expDateTime)

      val now = LocalDateTime.now()
      now.isAfter(expDateTime)
    }


  }


}

