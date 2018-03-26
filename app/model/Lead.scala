package model

import java.time.LocalDateTime

import play.api.libs.json.Json

/**
 * Created by fsznajderman on 24/01/2017.
 */
case class Lead(idApplicant: String, idTarget: String, dateTime: LocalDateTime = LocalDateTime.now())


case class LeadGluon(
  slug: String,
  idAttendee: String,
  firstName: String,
  lastName: String,
  company: String,
  email: String,
  message: String,
  scanDateTime: LocalDateTime)


object LeadGluon {

  implicit val format = Json.format[LeadGluon]
}