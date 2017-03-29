package model

import java.time.LocalDateTime

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}


/**
  * Created by fsznajderman on 19/01/2017.
  * Here we have different version on Person.. The most recent , is je PersonJson that aim to manage the DevoxxUs use case
  */
case class Person(id: Option[String], json: String = "")

case class LightingPerson(id: String, firstname: String, lastname: String)

case class CompletePerson(regId: String, firstname: String, lastname: String, gender: Option[String], title: Option[String], email: String, phoneNumber: Option[String], company: Option[String], json: String, datetime: Option[LocalDateTime])

case class CompletePersonWithNotes(person: CompletePerson, notes: Seq[LeadNote] = Nil)

case class PersonJson(regId: String, firstname: String, lastname: String, gender: Option[String], email: String, company: Option[String], city: Option[String], phone: Option[String], title: Option[String], isTraining: String, ticketFamily: Option[String], ticketType: Option[String], isRepresentative: String)


object Person {


  type JsonPerson = String

  def lightingPerson(p: Person): LightingPerson = LightingPerson(p.id.get.toString, "tobecompleted", "tobecompleted")

  def completePerson(p: JsonPerson): CompletePerson = {
    val pj = Json.parse(p).as[PersonJson]
    CompletePerson(pj.regId, pj.firstname, pj.lastname, pj.gender, pj.title, pj.email, pj.phone, pj.company, p, None)
  }


  implicit val personJsonReader: Reads[PersonJson] = (
    (__ \ "RegId").read[String] and
      (__ \ "first_Name").read[String] and
      (__ \ "last_Name").read[String] and
      (__ \ "gender").readNullable[String] and
      (__ \ "Email_Address").read[String] and
      (__ \ "Company").readNullable[String] and
      (__ \ "City").readNullable[String] and
      (__ \ "Phone").readNullable[String] and
      (__ \ "Title").readNullable[String] and
      (__ \ "isTraining").read[String] and
      (__ \ "Ticket_family").readNullable[String] and
      (__ \ "Ticket_type").readNullable[String] and
      (__ \ "isRepresentative").read[String]
    ) (PersonJson.apply _)


  implicit val personJWrites: Writes[PersonJson] = (

    (JsPath \ "RegId").write[String] and
      (JsPath \ "first_Name").write[String] and
      (JsPath \ "last_Name").write[String] and
      (JsPath \ "gender").writeNullable[String] and
      (JsPath \ "Email_Address").write[String] and
      (JsPath \ "Company").writeNullable[String] and
      (JsPath \ "City").writeNullable[String] and
      (JsPath \ "Phone").writeNullable[String] and
      (JsPath \ "Title").writeNullable[String] and
      (JsPath \ "isTraining").write[String] and
      (JsPath \ "Ticket_family").writeNullable[String] and
      (JsPath \ "Ticket_type").writeNullable[String] and
      (JsPath \ "isRepresentative").write[String]) (unlift(PersonJson.unapply))


  def json2PersonJson(json: String): PersonJson = {

    Json.parse(json).as[PersonJson]
  }

}
