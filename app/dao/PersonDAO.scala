package dao

import java.sql.Connection

import anorm.SqlParser.get
import anorm.{NamedParameter, RowParser, _}
import model.Person

/**
  * Created by fsznajderman on 19/01/2017.
  */
object PersonDAO extends mainDBDAO[Person, Long] {
  override def table: String = "person"

  override def getParams(item: Person): Seq[NamedParameter] = Seq[NamedParameter](
    'id -> item.id.get,
    'firstname -> item.firstname,
    'lastname -> item.lastname,
    'gender -> item.gender,
    'position -> item.gender,
    'status -> item.status,
    'experience -> item.experience,
    'isTraining -> item.isTraining,
    'showSensitive -> item.showSensitive,
    'profilid -> item.profil


  )

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


  def findAllLeadById(id: Long)(implicit c: Connection): Seq[Person] = {

    SQL"""
         SELECT * FROM PERSON p inner join LEAD l on p.id=l.idTarget WHERE l.idApplicant=$id
       """.as(rowParser.*)

  }

  def all(implicit connection: Connection): Seq[Person] = {

    SQL"""select * from Person""".as(rowParser.*)

  }



}
