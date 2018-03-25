package model

import play.api.libs.json.Json

/**
  * Created by fsznajderman on 11/02/2017.
  */
case class Sponsor(id: Option[Long]=None, slug:String, name: String, level: String)


case class SponsorItem(slug:String, name: String, level: String)

object SponsorItem{
  implicit val format = Json.format[SponsorItem]
}


case class EventDevoxx(name:String, slug:String, sponsors:Seq[SponsorItem])

object EventDevoxx{

  implicit val format = Json.format[EventDevoxx]
}




