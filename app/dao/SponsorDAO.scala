package dao

import java.sql.Connection

import anorm.SqlParser.get
import anorm.{NamedParameter, RowParser, _}
import model.Sponsor


/**
  * Created by fsznajderman on 11/02/2017.
  */
object SponsorDAO extends mainDBDAO[Sponsor, Long] {
  override def rowParser: RowParser[Sponsor] =
    for {
      id <- get[Option[Long]]("id")
      name <- get[String]("name")
      level <- get[String]("level")

    } yield
      Sponsor(
        id,
        name,
        level
      )

  override def table: String = "sponsor"

  override def getParams(item: Sponsor): Seq[NamedParameter] = Seq[NamedParameter](
    'name -> item.name,
    'level -> item.level
  )


  def all(implicit connection: Connection): Seq[Sponsor] = {

    SQL"""select * from SPONSOR """.as(rowParser.*)

  }


  def addRepresentative(idPerson: Long, idSponsor: Long)(implicit connection: Connection): Unit = {

    SQL"""insert into PERSON_SPONSOR (idperson, idsponsor) values ($idPerson, $idSponsor)""".execute()

  }

  case class PersonSponsorInfo(idPerson: Long, firstname: String, lastname: String, idSponsor: Option[Long], nameSponsor: Option[String])

  val personSponsorInfo = Macro.namedParser[PersonSponsorInfo]


  def allWithSponsor(implicit connection: Connection): Seq[PersonSponsorInfo] = {

    SQL"""select p.id as idPerson,  p.firstname, p.lastname,s.id as idSponsor, s."name" as nameSponsor  from SPONSOR s inner join
       PERSON_SPONSOR ps on s.id=ps.idSponsor
        right join PERSON p on p.id=ps.idperson""".as(personSponsorInfo.*)

  }


  def deleteRepresentative(idPerson: Long)(implicit connection: Connection): Unit = {
    SQL"""delete from PERSON_SPONSOR where idPerson=$idPerson""".execute
  }


  def personBySponsor(idSponsor: Long)(implicit connection: Connection): Seq[String] = {

    import anorm.SqlParser._

    SQL"""select p1.json from lead l inner join  person p1 on l.idtarget=p1.id where l.idapplicant in (
 select p.id from sponsor s inner join person_sponsor ps on  s.id=ps.idsponsor
  inner join person p on ps.idperson=p.id
  where s.id=${idSponsor})""".as(scalar[String].*)
  }
}
