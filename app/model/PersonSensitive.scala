package model

/**
  * Created by fsznajderman on 23/01/2017.
  */
case class  PersonSensitive(id: Option[Long] = None, email: String, phoneNumber: String, company: String, workLocation: String, lookingForAJob: Boolean)
