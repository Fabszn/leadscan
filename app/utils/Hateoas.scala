package utils

import model.{ErrorMessage, InfoMessage, Notification, Person}
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.Request
import utils.HateoasConverter.Converter
import utils.HateoasUtils.{Item, Links}

/**
  * Created by fsznajderman on 19/01/2017.
  */
object HateoasUtils {


  implicit def toHateoas[A](a: A)(implicit converter: Converter[A], request: Request[_]): JsObject = {
    converter.convert(a)
  }


  case class Item(k: String, v: String, isHref: Boolean = false)


  case class Links(items: Seq[Item]) {
    val name = "links"
  }


  def linkWrites(ls: Links)(implicit request: Request[_]): JsObject = {
    JsObject(ls.items.map {
      case item@Item(_, _, true) => item.k -> JsString(s"${href(request)}${item.v}")
      case item@Item(_, _, false) => item.k -> JsString(s"${item.v}")
    }.toMap)
  }


  private def href(request: Request[_]): String = {
    s"http://${request.host}"
  }

}

object HateoasConverter {

  trait Converter[A] {

    def name: String

    def convertMap(a: A): Map[String, String]

    def links(a: A): Seq[Links]

    def convert(a: A)(implicit request: Request[_]): JsObject = {
      import play.api.libs.json._
      val p = JsObject(Map(name -> JsObject(convertMap(a).map(i => i._1 -> JsString(i._2)))))

      links(a) match {
        case Nil => p
        case _ => p ++ JsObject(Map("links" -> JsArray(links(a).map(HateoasUtils.linkWrites))))
      }
    }
  }

  implicit object PersonConverter extends Converter[Person] {

    override def name: String = "person"

    override def convertMap(person: Person): Map[String, String] = Map(
      "firstname" -> person.firstname,
      "lastname" -> person.lastname,
      "gender" -> person.gender,
      "position" -> person.position,
      "status" -> person.status,
      "experience" -> person.experience.toString,
      "isTraining" -> person.isTraining.toString,
      "showSensitive" -> person.showSensitive.toString,
      "profil" -> person.profil.toString
    )


    override def links(person: Person): Seq[Links] = Seq(
      Links(Seq(Item("rel", "self"), Item("href", s"/persons/${person.id.get}",isHref = true))),
      Links(Seq(Item("rel", "contacts"), Item("href", s"/persons/${person.id.get}/contacts",isHref = true))),
      Links(Seq(Item("rel", "sensitive"), Item("href", s"/persons/${person.id.get}/sensitive",isHref = true)))
    )
  }

  case class NotificationConverter(notification: Notification) extends Converter[Notification] {

    override def name: String = "notification"

    override def convertMap(notification: Notification): Map[String, String] = Map()

    override def links(notification: Notification): Seq[Links] = ???
  }

  implicit object ErrorConverter extends Converter[ErrorMessage] {
    override def name: String = "error"

    override def convertMap(a: ErrorMessage): Map[String, String] = {
      Map("code" -> a.code,
        "message" -> a.message
      )
    }

    override def links(a: ErrorMessage): Seq[Links] = Seq()
  }

  implicit object MessageConverter extends Converter[InfoMessage] {
    override def name: String = "info"

    override def convertMap(a: InfoMessage): Map[String, String] = {
      Map("message" -> a.message)
    }

    override def links(a: InfoMessage): Seq[Links] = Seq()
  }

}
