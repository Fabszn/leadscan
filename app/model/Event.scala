package model

import java.time.LocalDateTime

/**
  * Created by fsznajderman on 19/03/2017.
  */
case class Event(id: Option[Long] = None, typeEvent: String, message: String, datetime: LocalDateTime = LocalDateTime.now())


sealed trait EventType {
  val typeEvent: String
}

case object ImportRepresentative extends EventType {
  override val typeEvent: String = "import_representative"

}

case object AddRepresentative extends EventType {
  override val typeEvent: String = "add_representative"

}

case object ImportRegistration extends EventType {
  override val typeEvent: String = "import_registration"

}

case object SendPassword extends EventType {
  override val typeEvent: String = "send_password"

}

case object SynchMyDevoxx extends EventType {
  override val typeEvent: String = "Syncho_MyDevoxx"

}

case object UpdateLeadNote extends EventType {
  override val typeEvent: String = "Update lead Note"

}

case object DeleteLead extends EventType {
  override val typeEvent: String = "Delete lead"

}

case object CreateLead extends EventType {
  override val typeEvent: String = "Create lead"

}

case object Login extends EventType {
  override val typeEvent: String = "Log in"

}

