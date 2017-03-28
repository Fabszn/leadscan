package controllers

import config.Settings
import pdi.jwt.{Jwt, JwtAlgorithm}
import play.api.libs.json._
import play.api.mvc.Request
import utils.LoggerAudit

import scala.util.{Failure, Success}

/**
  * Created by fsznajderman on 22/01/2017.
  */
package object jsonUtils extends LoggerAudit {


  trait JsonExtractor[A] {

    def convertor(js: JsValue, k: String): Option[A]


    def getConvertedMap(keys: List[String], json: JsValue): Map[String, Option[A]] =
      keys.map(k => k -> convertor(json, k)).filter {
        case (_, Some(_)) => true
        case (_, None) => false
      }.toMap
  }


  object JsonExtractor {


    implicit object StringJsonExtractor extends JsonExtractor[String] {
      override def convertor(js: JsValue, k: String): Option[String] = (js \ k).asOpt[String]
    }

    implicit object IntJsonExtractor extends JsonExtractor[Int] {
      override def convertor(js: JsValue, k: String): Option[Int] = (js \ k).asOpt[Int]
    }

    implicit object BooleanJsonExtractor extends JsonExtractor[Boolean] {
      override def convertor(js: JsValue, k: String): Option[Boolean] = (js \ k).asOpt[Boolean]
    }

  }


  def jsonToMapExtractor[A](keys: List[String], json: JsValue)(implicit jse: JsonExtractor[A]) = {
    jse.getConvertedMap(keys, json)
  }

  def tokenExtractorFromSession[A](request: Request[A]): String = {

    logger.info(s"${request.session.get("token")}")
    request.session.get("token").getOrElse {
      logger.error("tokenExtractor from session -> None Foken Found !! ")
      "no_token_found"
    }
  }

  def tokenExtractorFromHeader[A](request: Request[A]): String = {
    request.headers.get(Settings.oAuth.TOKEN_KEY).getOrElse {
      logger.error("tokenExtractor from header -> None Foken Found !! ")
      "no_token_found"
    }
  }


  def extractRegIdFromTokenRequest[A](request: Request[A]): String = {

    val token = tokenExtractorFromHeader(request)
    logger.info(s"Token found $token")

    regIdExtractor(token)
  }


  def regIdExtractor(token: String) = {
    Jwt.decode(token, Settings.oAuth.sharedSecret, Seq(JwtAlgorithm.HS256)) match {
      case Success(s) => (Json.parse(s) \ "registrantId").as[String]
      case Failure(es) => {
        logger.error(s"No RegistrantId found in token : $es")
        "-1"
      }
    }
  }
  def regIdExtractorReports(token: String) = {
    Jwt.decode(token, Settings.oAuth.localSecret, Seq(JwtAlgorithm.HS256)) match {
      case Success(s) => (Json.parse(s) \ "registrantId").as[String]
      case Failure(es) => {
        logger.error(s"No RegistrantId found in token : $es")
        "-1"
      }
    }
  }

  def emailExtractor(token: String) = {
    Jwt.decode(token, Settings.oAuth.sharedSecret, Seq(JwtAlgorithm.HS256)) match {
      case Success(s) => (Json.parse(s) \ "email").as[String]
      case Failure(es) => {
        logger.error(s"No email found in token : $es")
        "email not found"
      }
    }
  }

}





