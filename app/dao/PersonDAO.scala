package dao

import java.sql.Connection
import java.time.LocalDateTime

import anorm.SqlParser._
import anorm.{NamedParameter, RowParser, _}
import model.{CompletePerson, Person, PersonJson}

/**
  * Created by fsznajderman on 19/01/2017.
  */
object PersonDAO extends mainDBDAO[Person, String] {

  override def table: String = "person"

  override def getParams(item: Person): Seq[NamedParameter] = Seq[NamedParameter](
    'id -> item.id.get,
    'json -> item.json
  )

  override def rowParser: RowParser[Person] =
    for {
      id <- get[Option[String]]("id")
      json <- get[String]("json")
    } yield
      Person(
        id,
        json
      )

  def rowParserCompletePerson: RowParser[(String, LocalDateTime)] =
    for {
      json <- get[String]("json")
      datetime <- get[LocalDateTime]("datetime")
    } yield
      (
        json,
        datetime
      )

  //private val personInfo = Macro.namedParser[Person]

  def findAllLeadById(id: String)(implicit c: Connection): Seq[CompletePerson] = {

    SQL"""select * from PERSON p
       inner join lead l on l.idtarget=p.id
       where l.idapplicant = $id;""".as(rowParserCompletePerson.*).map(p => {

      Person.completePerson(p._1).copy(datetime = Option(p._2))

    })

  }

  def findAllLatestLeadById(id: String, datetime: LocalDateTime)(implicit c: Connection): Seq[CompletePerson] = {

    SQL"""select * from PERSON p inner join lead l on l.idtarget=p.id
       where l.idapplicant = $id and l.datetime > ${datetime};""".as(rowParserCompletePerson.*).map(p => Person.completePerson(p._1).copy(datetime = Option(p._2)))

  }

  def all(implicit connection: Connection): Seq[Person] = {

    SQL"""select * from Person""".as(rowParser.*)

  }

  def nextId(implicit connection: Connection): Long = {
    SQL"""select max(id)+1 as nextId from person""".as(long("nextId").single)
  }

  case class Pass(regId: String, pass: String)

  private val passInfo = Macro.namedParser[Pass]

  def pass(implicit connection: Connection): Seq[Pass] = {
    SQL"""select * from pass""".as(passInfo *)
  }

  def addPass(regId: String, pass: String)(implicit connection: Connection): Unit = {
    SQL"""insert into pass (regId, pass) values($regId, $pass)""".execute()
  }


}
