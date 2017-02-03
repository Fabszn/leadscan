package model

import java.time.LocalDateTime

/**
  * Created by fsznajderman on 24/01/2017.
  */
case class Lead(idApplicant: Long, idTarget: Long, dateTime: LocalDateTime = LocalDateTime.now())


