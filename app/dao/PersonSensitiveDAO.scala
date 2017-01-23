package dao

import java.sql.Connection

import anorm.SqlParser.get
import anorm.{NamedParameter, RowParser, _}
import model.PersonSensitive

/**
  * Created by fsznajderman on 23/01/2017.
  */
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

  override def getParams(item: PersonSensitive): Seq[NamedParameter] = ???


  def getSensitiveDataByIdPerson(idPerson: Long)(implicit conn: Connection): Option[PersonSensitive] = {

    SQL"""SELECT ps.* FROM person p
         inner join person_sensitive ps on p.sensitiveId=ps.id
         where p.id=${idPerson}""".as(rowParser.singleOpt)


  }
}
