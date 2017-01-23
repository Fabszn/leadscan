package dao

import anorm.SqlParser.get
import anorm.{NamedParameter, RowParser}
import model.Person

/**
  * Created by fsznajderman on 19/01/2017.
  */
object PersonDAO extends mainDBDAO[Person, Long] {
  override def table: String = "person"

  override def getParams(item: Person): Seq[NamedParameter] = ???

  override def rowParser: RowParser[Person] =
    for {
      id <- get[Option[Long]]("id")
      firstname <- get[String]("firstname")
      lastname <- get[String]("lastname")
      gender <- get[String]("gender")
      position <- get[String]("position")
      status <- get[String]("status")
      experience <- get[Int]("experience")
      isTraining <- get[Boolean]("isTraining")
      showSensitive <- get[Boolean]("showSensitive")
      profil <- get[Int]("profilid")
    } yield
      Person(
        id,
        firstname,
        lastname,
        gender,
        position,
        status,
        experience,
        isTraining,
        showSensitive,
        profil
      )
}
