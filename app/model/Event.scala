package model

import java.time.LocalDateTime

/**
  * Created by fsznajderman on 19/03/2017.
  */
case class Event(id: Option[Long]=None, typeEvent: String, message: String, datetime: LocalDateTime=LocalDateTime.now())


sealed trait EventType{
  val typeEvent:String
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

