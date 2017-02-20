package utils

import model._
import play.api.libs.json._
import play.api.mvc.Request
import utils.HateoasConverter.Converter
import utils.HateoasUtils._

/**
  * Created by fsznajderman on 19/01/2017.
  */


object HateoasConverter {

  trait Converter[A] {

    def name: String

    def convertMap(a: A)(implicit request: Request[_]): Map[String, JsValue]

    def links(a: A): Seq[Links]

    def convert(a: A)(implicit request: Request[_]): JsObject = {
      import play.api.libs.json._
      val p = JsObject(Map(name -> JsObject(convertMap(a))))

      links(a) match {
        case Nil => p
        case _ => p ++ JsObject(Map("links" -> JsArray(links(a).map(HateoasUtils.linkWrites))))
      }
    }
  }


  implicit object PersonConverter extends Converter[Person] {

    override def name: String = "person"

    override def convertMap(person: Person)(implicit request: Request[_]): Map[String, JsValue] = person2Map(person)


    override def links(person: Person): Seq[Links] = Seq(
      Links(Seq(Item("rel", "self"), Item("href", s"/persons/${person.id.get}", isHref = true))),
      Links(Seq(Item("rel", "contacts"), Item("href", s"/persons/${person.id.get}/contacts", isHref = true))),
      Links(Seq(Item("rel", "sensitive"), Item("href", s"/persons/${person.id.get}/sensitive", isHref = true)))
    )
  }


  implicit object PersonSensitiveConverter extends Converter[PersonSensitive] {

    override def name: String = "person_sensitive"

    override def convertMap(pSensitive: PersonSensitive)(implicit request: Request[_]): Map[String, JsValue] = Map(
      "email" -> JsString(pSensitive.email),
      "company" -> JsString(pSensitive.company),
      "phoneNumber" -> JsString(pSensitive.phoneNumber),
      "workLocation" -> JsString(pSensitive.workLocation),
      "lookingForAJob" -> JsBoolean(pSensitive.lookingForAJob)
    )


    override def links(pSensitive: PersonSensitive): Seq[Links] = Seq(
      Links(Seq(Item("rel", "self"), Item("href", s"/persons/${pSensitive.id.get}/sensitive", isHref = true))),
      Links(Seq(Item("rel", "contacts"), Item("href", s"/persons/${pSensitive.id.get}/contacts", isHref = true))),
      Links(Seq(Item("rel", "person"), Item("href", s"/persons/${pSensitive.id.get}", isHref = true)))
    )
  }


  implicit object CompletePersonConverter extends Converter[CompletePerson] {

    override def name: String = "person"

    override def convertMap(a: CompletePerson)(implicit request: Request[_]): Map[String, JsValue] = completePerson2Map(a)

    override def links(a: CompletePerson): Seq[Links] = Seq(
      Links(Seq(Item("rel", "self"), Item("href", s"/persons/${a.id.get}/sensitive", isHref = true))),
      Links(Seq(Item("rel", "contacts"), Item("href", s"/persons/${a.id.get}/contacts", isHref = true))),
      Links(Seq(Item("rel", "person"), Item("href", s"/persons/${a.id.get}", isHref = true))))
  }


  implicit object LeadsConverter extends Converter[Seq[CompletePerson]] {
    override def name: String = "leads"

    override def convertMap(a: Seq[CompletePerson])(implicit request: Request[_]): Map[String, JsValue] = {
      import play.api.libs.json._
      a.map(p => s"person_${p.id.get}" -> {
        JsObject(completePerson2Map(p)) ++ JsObject(Map("links" -> JsArray(CompletePersonConverter.links(p).map(HateoasUtils.linkWrites))))
      }).toMap
    }

    override def links(a: Seq[CompletePerson]): Seq[Links] = Seq()
  }


  implicit object NotificationConverter extends Converter[Notification] {

    override def name: String = "notification"

    override def convertMap(notification: Notification)(implicit request: Request[_]): Map[String, JsValue] = notification2Map(notification)

    override def links(notification: Notification): Seq[Links] = Seq(
      Links(Seq(Item("rel", "self"), Item("href", s"/notifications/${notification.id.get}", isHref = true))),
      Links(Seq(Item("rel", "requester"), Item("href", s"/persons/${notification.idRequester}", isHref = true))),
      Links(Seq(Item("rel", "recipient"), Item("href", s"/persons/${notification.idRecipient}", isHref = true))))
  }


  implicit object NotificationsConverter extends Converter[Seq[Notification]] {
    override def name: String = "notifications"

    override def convertMap(a: Seq[Notification])(implicit request: Request[_]): Map[String, JsValue] = {
      import play.api.libs.json._
      a.map(n => s"notification_${n.id.get}" -> {
        JsObject(notification2Map(n)) ++ JsObject(Map("links" -> JsArray(NotificationConverter.links(n).map(HateoasUtils.linkWrites))))
      }).toMap


    }

    override def links(a: Seq[Notification]): Seq[Links] = Seq()
  }

  implicit object ErrorConverter extends Converter[ErrorMessage] {
    override def name: String = "error"

    override def convertMap(a: ErrorMessage)(implicit request: Request[_]): Map[String, JsValue] = {
      Map("code" -> JsString(a.code),
        "message" -> JsString(a.message)
      )
    }

    override def links(a: ErrorMessage): Seq[Links] = Seq()
  }

  implicit object MessageConverter extends Converter[InfoMessage] {
    override def name: String = "info"

    override def convertMap(a: InfoMessage)(implicit request: Request[_]): Map[String, JsValue] = {
      Map("message" -> JsString(a.message))
    }

    override def links(a: InfoMessage): Seq[Links] = Seq()
  }


  implicit object LeadNoteConverter extends Converter[LeadNote] {
    override def name: String = "Lead_note"

    override def convertMap(a: LeadNote)(implicit request: Request[_]): Map[String, JsValue] = leadNote2Map(a)

    override def links(a: LeadNote): Seq[Links] = Seq(
      Links(Seq(Item("rel", "self"), Item("href", s"/leads/note/${a.id.get}", isHref = true))),
      Links(Seq(Item("rel", "requester"), Item("href", s"/persons/${a.idApplicant}", isHref = true))),
      Links(Seq(Item("rel", "recipient"), Item("href", s"/persons/${a.idTarget}", isHref = true)))
    )

  }

  implicit object leadNotesConverter extends Converter[Seq[LeadNote]] {
    override def name: String = "Lead_notes"

    override def convertMap(a: Seq[LeadNote])(implicit request: Request[_]): Map[String, JsValue] = {
      import play.api.libs.json._
      a.map(n => s"lead_note${n.id.get}" -> {
        JsObject(leadNote2Map(n)) ++ JsObject(Map("links" -> JsArray(LeadNoteConverter.links(n).map(HateoasUtils.linkWrites))))
      }).toMap
    }

    override def links(a: Seq[LeadNote]): Seq[Links] = Seq()
  }

}


object HateoasUtils extends LoggerAudit {


  implicit def toHateoas[A](a: A)(implicit converter: Converter[A], request: Request[_]): JsObject = {
    converter.convert(a)
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

  def person2Map(person: Person): Map[String, JsValue] = {
    import model.Person.personJsonReader
    Json.fromJson[PersonJson](Json.parse(person.json)).asEither match {
      case Left(e) =>

        logger.error("error " + e)
        Map(
          "firstname" -> JsString(person.firstname),
          "lastname" -> JsString(person.lastname),
          "gender" -> JsString(person.gender),
          "position" -> JsString(person.position),
          "status" -> JsString(person.status),
          "experience" -> JsNumber(person.experience),
          "isTraining" -> JsBoolean(person.isTraining),
          "showSensitive" -> JsBoolean(person.showSensitive)
        )
      case Right(personJ) =>
        personJson2Map(personJ)
    }
  }

  def completePerson2Map(person: CompletePerson): Map[String, JsValue] = {
    import model.Person.personJsonReader
    Json.fromJson[PersonJson](Json.parse(person.json)).asEither match {
      case Left(e) =>

        logger.error("error " + e)
        Map(
          "firstname" -> JsString(person.firstname),
          "lastname" -> JsString(person.lastname),
          "gender" -> JsString(person.gender),
          "position" -> JsString(person.position),
          "status" -> JsString(person.status),
          "experience" -> JsNumber(person.experience),
          "isTraining" -> JsBoolean(person.isTraining),
          "showSensitive" -> JsBoolean(person.showSensitive),
          "profil" -> JsNumber(person.profilId),
          "email" -> JsString(person.email),
          "company" -> JsString(person.company),
          "phoneNumber" -> JsString(person.phoneNumber),
          "workLocation" -> JsString(person.workLocation),
          "lookingForAJob" -> JsBoolean(person.lookingForAJob)
        )
      case Right(personJ) =>
        personJson2Map(personJ)
    }
  }

  private def personJson2Map(personJ: PersonJson) = {
    Map(
      "firstname" -> JsString(personJ.firstname),
      "lastname" -> JsString(personJ.lastname),
      "email" -> JsString(personJ.email),
      "address1" -> JsString(personJ.address1.getOrElse("")),
      "address1" -> JsString(personJ.address2.getOrElse("")),
      "city" -> JsString(personJ.city.getOrElse("")),
      "country" -> JsString(personJ.country.getOrElse("")),
      "fax" -> JsString(personJ.fax.getOrElse("")),
      "phone" -> JsString(personJ.phone.getOrElse("")),
      "region" -> JsString(personJ.region.getOrElse("")),
      "title" -> JsString(personJ.title.getOrElse("")),
      "codePostal" -> JsString(personJ.postalCode.getOrElse("")),
      "company" -> JsString(personJ.company.getOrElse(""))
    )
  }

  def personSensitive2Map(pSensitive: PersonSensitive): Map[String, JsValue] = {
    Map(
      "email" -> JsString(pSensitive.email),
      "company" -> JsString(pSensitive.company),
      "phoneNumber" -> JsString(pSensitive.phoneNumber),
      "workLocation" -> JsString(pSensitive.workLocation),
      "lookingForAJob" -> JsBoolean(pSensitive.lookingForAJob)
    )
  }

  def notification2Map(notification: Notification): Map[String, JsValue] = {
    Map(
      "idRecipient" -> JsNumber(notification.idRecipient),
      "idRequester" -> JsNumber(notification.idRequester),
      "typeNotif" -> JsNumber(notification.typeNotif),
      "status" -> JsNumber(notification.status.id),
      "dateTimee" -> JsString(notification.dateTime.toString)
    )
  }

  def leadNote2Map(note: LeadNote): Map[String, JsValue] = {
    Map(
      "idApplicant" -> JsNumber(note.idApplicant),
      "idTarget" -> JsNumber(note.idTarget),
      "note" -> JsString(note.note),
      "id" -> JsNumber(note.id.get),
      "dateTimee" -> JsString(note.dateTime.toString)
    )
  }

  case class Item(k: String, v: String, isHref: Boolean = false)

  case class Links(items: Seq[Item]) {
    val name = "links"
  }

}
