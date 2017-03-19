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

  case class PersonSponsorInfo(idPerson: Long, firstname: String, lastname: String, email: String = "", idSponsor: Option[Long], nameSponsor: Option[String])


  def allWithSponsor(implicit connection: Connection): Seq[PersonSponsorInfo] = {

    val personSponsorInfoRowParser = Macro.namedParser[PersonSponsorInfo]

    SQL"""select p.id as idPerson,  p.firstname, p.lastname,'' as email, s.id as idSponsor, s."name" as nameSponsor  from SPONSOR s inner join
       PERSON_SPONSOR ps on s.id=ps.idSponsor
        right join PERSON p on p.id=ps.idperson""".as(personSponsorInfoRowParser.*)

  }

  def onlyRepresentatives(idSponsor:Long)(implicit connection: Connection): Seq[PersonSponsorInfo] = {

    val personSponsorInfoRowParser = Macro.namedParser[PersonSponsorInfo]

    SQL"""select p.id as idPerson
       ,p.firstname
       , p.lastname
       ,pse.email
       ,s.id as idSponsor, s."name" as nameSponsor  from
       SPONSOR s inner join
       PERSON_SPONSOR ps on s.id=ps.idSponsor inner join
       PERSON p on p.id=ps.idperson inner join
       person_sensitive pse on pse.id=p.id where s.id=${idSponsor}""".as(personSponsorInfoRowParser.*)

  }


  def deleteRepresentative(idPerson: Long)(implicit connection: Connection): Unit = {
    SQL"""delete from PERSON_SPONSOR where idPerson=$idPerson""".execute
  }


  case class LeadLine(idApplicant: Long, sponsor: String = "", json: String)

  case class LeadLineWithNote(name: String = "", json: String, note: String)


  val LeadLineRowParser = Macro.namedParser[LeadLine]
  val LeadLineWithNoteRowParser = Macro.namedParser[LeadLineWithNote]

  def personBySponsor(idSponsor: Long)(implicit connection: Connection): Seq[LeadLine] = {

    SQL"""select l.idapplicant, '' as sponsor,p1.json from lead l inner join  person p1 on l.idtarget = p1.id where l.idapplicant in (
 select p.id from sponsor s inner join person_sponsor ps on  s.id=ps.idsponsor
  inner join person p on ps.idperson=p.id
  where s.id=${idSponsor})""".as(LeadLineRowParser.*)
  }

  def personByRepresentative(idRepresentative: Long)(implicit connection: Connection): Seq[LeadLineWithNote] = {

    SQL"""select s.name, p.json, ln.note from lead l
         inner join person p on l.idtarget = p.id
         inner join lead_note ln on (l.idtarget=ln.idtarget and l.idapplicant=ln.idapplicant)
         inner join person_sponsor ps on ps.idperson = l.idapplicant
         inner join sponsor s on s.id = ps.idsponsor
         where l.idapplicant=${idRepresentative}""".as(LeadLineWithNoteRowParser.*)
  }


  def allPersonScanned(implicit connection: Connection): Seq[LeadLine] = {

    SQL"""select distinct l.idapplicant, s.name as sponsor ,p1.json from lead l
  inner join  person p1 on l.idtarget = p1.id inner join
  lead l2 on l.idapplicant=l2.idapplicant inner join person_sponsor ps  on ps.idperson=l2.idapplicant
  inner join sponsor s on ps.idsponsor=s.id""".as(LeadLineRowParser.*)
  }


}
