package model

import java.time.LocalDateTime

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}


/**
  * Created by fsznajderman on 19/01/2017.
  * Here we have different version on Person.. The most recent , is PersonJson that aim to manage the DevoxxUs use case
  */
case class Person(id: Option[String], json: String = "")

case class LightingPerson(id: String, firstname: String, lastname: String)

case class CompletePerson(regId: String, firstname: String, lastname: String, title: String, email: String, phoneNumber: Option[String], company: String, json: String, datetime: Option[LocalDateTime])

case class CompletePersonWithNotes(person: CompletePerson, notes: Seq[LeadNote] = Nil)

case class PersonJson(regId: String,
                      gender: Option[String],
                      firstname: String,
                      lastname: String,
                      email: String,
                      title: String,
                      company: String,
                      workAdress1: Option[String],
                      workAdress2: Option[String],
                      city: Option[String],
                      workCounty: Option[String],
                      WorkPostCode: Option[String],
                      workCountry: Option[String],
                      phone: Option[String])

//RegId,gender,firstname,lastname,email,title,company,workAdress1,workAdress2,city,workCounty,WorkPostCode,workCountry,phone

object Person {


  type JsonPerson = String

  def lightingPerson(p: Person): LightingPerson = LightingPerson(p.id.get.toString, "tobecompleted", "tobecompleted")

  def completePerson(p: JsonPerson): CompletePerson = {
    val pj = Json.parse(p).as[PersonJson]
    CompletePerson(pj.regId, pj.firstname, pj.lastname, pj.title, pj.email, pj.phone, pj.company, p, None)
  }


  //RegId,Prefix,first_Name,last_Name,Email,Job_Title,Company,Work_Address_1,Work_Address_2,Work_City,Work_County,Work_Postcode,Work_Country,Work_Phone


  implicit val personJsonReader: Reads[PersonJson] = (
    (__ \ "RegId").read[String] and
    (__ \ "gender").readNullable[String] and
      (__ \ "firstname").read[String] and
      (__ \ "lastname").read[String] and
      (__ \ "email").read[String] and
      (__ \ "title").read[String] and
      (__ \ "company").read[String] and
      (__ \ "workAdress1").readNullable[String] and
      (__ \ "workAdress2").readNullable[String] and
      (__ \ "city").readNullable[String] and
      (__ \ "workCounty").readNullable[String] and
      (__ \ "WorkPostCode").readNullable[String] and
      (__ \ "workCountry").readNullable[String] and
      (__ \ "phone").readNullable[String]
    ) (PersonJson.apply _)


  implicit val personJWrites: Writes[PersonJson] = (

    (JsPath \ "RegId").write[String] and
      (JsPath \ "gender").writeNullable[String] and
      (JsPath \ "firstname").write[String] and
      (JsPath \ "lastname").write[String] and
      (JsPath \ "email").write[String] and
      (JsPath \ "title").write[String] and
      (JsPath \ "Company").write[String] and
      (JsPath \ "workAdress1").writeNullable[String] and
      (JsPath \ "workAdress2").writeNullable[String] and
      (JsPath \ "city").writeNullable[String] and
      (JsPath \ "workCounty").writeNullable[String] and
      (JsPath \ "WorkPostCode").writeNullable[String] and
      (JsPath \ "workCountry").writeNullable[String] and
      (JsPath \ "phone").writeNullable[String]) (unlift(PersonJson.unapply))


  def json2PersonJson(json: String): PersonJson = {

    Json.parse(json).as[PersonJson]
  }

}
