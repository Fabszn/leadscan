package model

/**
  * Created by fsznajderman on 19/01/2017.
  */
case class Person(id: Option[Long], firstname: String, lastname: String, gender: String, position: String, status: String, experience: Int, isTraining: Boolean, showSensitive: Boolean, profil: Int) {

}
