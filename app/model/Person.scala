package model

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}


/**
  * Created by fsznajderman on 19/01/2017.
  * Here we have different version on Person.. The most recent , is je PersonJson that aim to manage the DevoxxUs use case
  */
case class Person(id: Option[Long], firstname: String, lastname: String, gender: String, position: String, status: String, experience: Int, isTraining: Boolean, showSensitive: Boolean, profil: Int, json: String = "")

case class LightingPerson(id: String, firstname: String, lastname: String)

case class CompletePerson(id: Option[Long], firstname: String, lastname: String, gender: String, position: String, status: String, experience: Int, isTraining: Boolean, showSensitive: Boolean, profilId: Int, email: String, phoneNumber: String, company: String, workLocation: String, lookingForAJob: Boolean, json: String = "")


case class PersonJson(regId: String, firstname: String, lastname: String, email: String, company: Option[String], address1: Option[String], address2: Option[String], city: Option[String], region: Option[String], postalCode: Option[String], country: Option[String], phone: Option[String], fax: Option[String], title: Option[String])


object Person {


  def lightingPerson(p: Person): LightingPerson = LightingPerson(p.id.get.toString, p.firstname, p.lastname)


  implicit val personJsonReader: Reads[PersonJson] = (
    (__ \ "\uFEFFRegId").read[String] and
      (__ \ "first_Name").read[String] and
      (__ \ "last_Name").read[String] and
      (__ \ "Email_Address").read[String] and
      (__ \ "company").readNullable[String] and
      (__ \ "Address_1").readNullable[String] and
      (__ \ "Address_2").readNullable[String] and
      (__ \ "city").readNullable[String] and
      (__ \ "Region").readNullable[String] and
      (__ \ "postal_Code").readNullable[String] and
      (__ \ "Country").readNullable[String] and
      (__ \ "Phone").readNullable[String] and
      (__ \ "Fax").readNullable[String] and
      (__ \ "Title").readNullable[String]
    ) (PersonJson.apply _)


  implicit val personJWrites: Writes[PersonJson] = (

    (JsPath \ "\uFEFFRegId").write[String] and
      (JsPath \ "first_Name").write[String] and
      (JsPath \ "last_Name").write[String] and
      (JsPath \ "Email_Address").write[String] and
      (JsPath \ "company").writeNullable[String] and
      (JsPath \ "Address_1").writeNullable[String] and
      (JsPath \ "Address_2").writeNullable[String] and
      (JsPath \ "city").writeNullable[String] and
      (JsPath \ "Region").writeNullable[String] and
      (JsPath \ "postal_Code").writeNullable[String] and
      (JsPath \ "Country").writeNullable[String] and
      (JsPath \ "Phone").writeNullable[String] and
      (JsPath \ "Fax").writeNullable[String] and
      (JsPath \ "Title").writeNullable[String]) (unlift(PersonJson.unapply))


}
