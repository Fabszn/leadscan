package model

/**
  * Created by fsznajderman on 19/01/2017.
  */
case class Person(id: Option[Long], firstname: String, lastname: String, gender: String, position: String, status: String, experience: Int, isTraining: Boolean, showSensitive: Boolean, profil: Int)

case class LightingPerson(id: String, firstname: String, lastname: String)

case class CompletePerson(id: Option[Long], firstname: String, lastname: String, gender: String, position: String, status: String, experience: Int, isTraining: Boolean, showSensitive: Boolean, profilId: Int, email: String, phoneNumber: String, company: String, workLocation: String, lookingForAJob: Boolean)

object Person {


  def lightingPerson(p: Person): LightingPerson = LightingPerson(p.id.get.toString, p.firstname, p.lastname)


}
