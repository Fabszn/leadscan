package dao

import java.sql.Connection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import anorm.SqlParser._
import anorm.{NamedParameter, RowParser, _}
import model.{CompletePerson, Person}

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
    'profilid -> item.profil,
    'json -> item.json


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
      json <- get[String]("json")
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
        profil,
        json
      )

  private val personCompleteInfo = Macro.namedParser[CompletePerson]

  def findAllLeadById(id: Long)(implicit c: Connection): Seq[CompletePerson] = {

    SQL"""select * from PERSON p inner join person_sensitive ps on p.id=ps.id
       inner join lead l on l.idtarget=p.id
       where l.idapplicant = $id;""".as(personCompleteInfo.*)

  }

  def findAllLatestLeadById(id: Long, datetime:LocalDateTime)(implicit c: Connection): Seq[CompletePerson] = {


    SQL"""select * from PERSON p inner join person_sensitive ps on p.id=ps.id
       inner join lead l on l.idtarget=p.id
       where l.idapplicant = $id and l.datetime > ${datetime};""".as(personCompleteInfo.*)

  }

  def all(implicit connection: Connection): Seq[Person] = {

    SQL"""select * from Person""".as(rowParser.*)

  }

  def nextId(implicit connection: Connection): Long = {
    SQL"""select max(id)+1 as nextId from person""".as(long("nextId").single)
  }



}
