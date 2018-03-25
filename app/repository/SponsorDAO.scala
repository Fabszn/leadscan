package repository

import java.sql.Connection

import anorm.SqlParser.get
import anorm.{NamedParameter, RowParser, _}
import model.{Person, PersonJson, Sponsor}


/**
  * Created by fsznajderman on 11/02/2017.
  */
object SponsorDAO extends mainDBDAO[Sponsor, Long] {

  override def rowParser: RowParser[Sponsor] =
    for {
      id <- get[Option[Long]]("id")
      slug <- get[String]("slug")
      name <- get[String]("name")
      level <- get[String]("level")
    } yield
      Sponsor(
        id,
        slug,
        name,
        level
      )

  def rowParserSponsorInfo: RowParser[(PersonJson, Option[Long], Option[String])] =
    for {
      json <- get[String]("json")
      idSponsor <- get[Option[Long]]("idSponsor")
      nameSponsor <- get[Option[String]]("nameSponsor")
    } yield {
      val pj = Person.json2PersonJson(json)
      (pj, idSponsor, nameSponsor)
    }


  override def table: String = "sponsor"

  override def getParams(item: Sponsor): Seq[NamedParameter] = Seq[NamedParameter](
    'name -> item.name,
    'level -> item.level,
    'slug -> item.slug
  )



  def all(implicit connection: Connection): Seq[Sponsor] = {
    SQL"""select * from SPONSOR """.as(rowParser.*)
  }

  def addRepresentative(idPerson: String, idSponsor: Long)(implicit connection: Connection): Unit = {
    SQL"""insert into PERSON_SPONSOR (idperson, idsponsor) values ($idPerson, $idSponsor)""".execute()
  }

  def isRepresentative(idPerson: String, idSponsor: Long)(implicit c:Connection): Boolean = {
    val res=SQL"""SELECT idPerson from PERSON_SPONSOR ps WHERE ps.idperson = ${idPerson} AND ps.idSponsor = ${idSponsor} LIMIT 1""".as(SqlParser.str("idPerson").singleOpt)
    res.isDefined
  }

  case class PersonSponsorInfo(idPerson: String, firstname: String, lastname: String, email: String = "", idSponsor: Option[String], nameSponsor: Option[String])


  def allWithSponsor(implicit connection: Connection): Seq[PersonSponsorInfo] = {

    SQL"""select json
         ,s.id as idSponsor, s."name" as nameSponsor  from SPONSOR s inner join
       PERSON_SPONSOR ps on s.id=ps.idSponsor
        right join PERSON p on p.id=ps.idperson""".as(rowParserSponsorInfo.*).map(data => PersonSponsorInfo(data._1.regId, data._1.firstname, data._1.lastname, email = "", data._2.map(_.toString), data._3))

  }

  def onlyRepresentatives(idSponsor: Long)(implicit connection: Connection): Seq[PersonSponsorInfo] = {

    SQL"""select json
       ,s.id as idSponsor, s."name" as nameSponsor  from
       SPONSOR s inner join
       PERSON_SPONSOR ps on s.id=ps.idSponsor inner join
       PERSON p on p.id=ps.idperson  where s.id=${idSponsor}""".as(rowParserSponsorInfo.*).map(data => PersonSponsorInfo(data._1.regId, data._1.firstname, data._1.lastname, data._1.email, data._2.map(_.toString), data._3))
  }

  def onlyRepresentatives(implicit connection: Connection): Seq[PersonSponsorInfo] = {

    val personSponsorInfoRowParser = Macro.namedParser[PersonSponsorInfo]

    SQL"""select json
       ,s.id as idSponsor, s."name" as nameSponsor  from
       SPONSOR s inner join
       PERSON_SPONSOR ps on s.id=ps.idSponsor inner join
       PERSON p on p.id=ps.idperson""".as(rowParserSponsorInfo.*).map(data => PersonSponsorInfo(data._1.regId, data._1.firstname, data._1.lastname, email = "", data._2.map(_.toString), data._3))

  }


  def deleteRepresentative(idPerson: String)(implicit connection: Connection): Unit = {
    SQL"""delete from PERSON_SPONSOR where idPerson=$idPerson""".execute
  }


  case class LeadLine(idApplicant: String, sponsor: String = "", json: String)

  case class LeadLineWithNote(name: String = "", json: String, note: String)


  val LeadLineRowParser = Macro.namedParser[LeadLine]
  val LeadLineWithNoteRowParser = Macro.namedParser[LeadLineWithNote]

  def personBySponsor(idSponsor: Long)(implicit connection: Connection): Seq[LeadLine] = {

    SQL"""select l.idapplicant, '' as sponsor,p1.json from lead l inner join  person p1 on l.idtarget = p1.id where l.idapplicant in (
 select p.id from sponsor s inner join person_sponsor ps on  s.id=ps.idsponsor
  inner join person p on ps.idperson=p.id
  where s.id=${idSponsor})""".as(LeadLineRowParser.*)
  }

  def personByRepresentative(idRepresentative: String)(implicit connection: Connection): Seq[LeadLineWithNote] = {

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

  def allPersonScannedBySponsor(id:Long)(implicit connection: Connection): Seq[LeadLine] = {

    SQL"""select distinct l.idapplicant, s.name as sponsor ,p1.json from lead l
  inner join  person p1 on l.idtarget = p1.id inner join
  lead l2 on l.idapplicant=l2.idapplicant inner join person_sponsor ps  on ps.idperson=l2.idapplicant
  inner join sponsor s on ps.idsponsor=s.id where s.id=${id}""".as(LeadLineRowParser.*)
  }

  def isRepresentative(regID: String)(implicit connection: Connection): Option[Sponsor] = {
      SQL"""select * from person_sponsor ps inner join sponsor s on ps.idsponsor=s.id where ps.idperson=${regID}""".as(rowParser.singleOpt)
  }



}
