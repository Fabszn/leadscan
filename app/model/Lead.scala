package model

import java.time.LocalDateTime

/**
  * Created by fsznajderman on 24/01/2017.
  */
case class Lead(idApplicant: String, idTarget: String, dateTime: LocalDateTime = LocalDateTime.now())


