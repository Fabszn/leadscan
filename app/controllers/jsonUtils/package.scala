package controllers

import play.api.libs.json._

/**
  * Created by fsznajderman on 22/01/2017.
  */
package object jsonUtils {


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
}





