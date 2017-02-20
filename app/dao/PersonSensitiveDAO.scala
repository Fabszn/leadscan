package dao

import java.sql.Connection

import anorm.SqlParser.{get, long}
import anorm.{NamedParameter, RowParser, _}
import model.PersonSensitive

/**
  * Created by fsznajderman on 23/01/2017.
  */
@deprecated
object PersonSensitiveDAO extends mainDBDAO[PersonSensitive, Long] {
  override def rowParser: RowParser[PersonSensitive] = for {
    id <- get[Option[Long]]("id")
    email <- get[String]("email")
    phoneNumber <- get[String]("phoneNumber")
    company <- get[String]("company")
    workLocation <- get[String]("workLocation")
    lookingForAJob <- get[Boolean]("lookingForAJob")
  } yield PersonSensitive(id, email, phoneNumber, company, workLocation, lookingForAJob)

  override def table: String = "person_sensitive"

  override def getParams(item: PersonSensitive): Seq[NamedParameter] = Seq[NamedParameter](
    'id -> item.id.get,
    'email -> item.email,
    'phonenumber -> item.phoneNumber,
    'company -> item.company,
    'workLocation -> item.workLocation,
    'lookingForAJob -> item.lookingForAJob
  )


  def getSensitiveDataByIdPerson(idPerson: Long)(implicit conn: Connection): Option[PersonSensitive] = {

    SQL"""SELECT ps.* FROM person p
         inner join person_sensitive ps on p.id=ps.id
         where p.id=${idPerson}""".as(rowParser.singleOpt)


  }

  def nextId(implicit connection: Connection):Long = {
    SQL"""select max(id)+1 as nextId from person_sensitive""".as(long("nextId").single)
  }
}
