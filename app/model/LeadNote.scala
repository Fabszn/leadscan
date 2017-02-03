package model

import java.time.LocalDateTime

/**
  * Created by fsznajderman on 03/02/2017.
  */
case class LeadNote(id: Option[Long] = None, idApplicant: Long, idTarget: Long, note: String, dateTime: LocalDateTime = LocalDateTime.now())

