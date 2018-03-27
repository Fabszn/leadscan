package repository

import java.sql.Connection
import java.time.LocalDateTime

import anorm.SqlParser._
import anorm.{NamedParameter, RowParser, _}
import model.{Lead, Person}

/**
  * Created by fsznajderman on 24/01/2017.
  */
object LeadDAO extends mainDBDAO[Lead, String] {

  override def rowParser: RowParser[Lead] = for {
    idApplicant <- get[String]("idApplicant")
    idTarget <- get[String]("idTarget")
    dateTime <- get[LocalDateTime]("dateTime")
  } yield Lead(idApplicant, idTarget, dateTime)


  override def table: String = "lead"

  override def getParams(item: Lead): Seq[NamedParameter] = Seq[NamedParameter](
    'idApplicant -> item.idApplicant,
    'idTarget -> item.idTarget,
    'dateTime -> item.dateTime
  )


  def findByPks(idApplicant: String, idTarget: String)(implicit c: Connection): Option[Lead] = {

    SQL"""
         SELECT * from LEAD where idApplicant=${idApplicant} and idTarget=$idTarget
       """.as(rowParser.singleOpt)
  }


  def leadBySponsor(implicit c: Connection): Seq[(Int, String)] = {

    val parser = for {
      nb <- get[Int]("nb")
      sponsot <- get[String]("sponsor")
    } yield (nb, sponsot)

    SQL(" select count(*) as nb, s.name as sponsor from SPONSOR s inner join PERSON_SPONSOR ps on s.id=ps.idsponsor inner join LEAD l on ps.idperson=l.idapplicant  group by s.name").as(parser.*)

  }


  def leadByhour(implicit c: Connection): Seq[Item] = {

    val rowParserStat: RowParser[Item] = for {
      nbLead <- get[Int]("nb")
      day <- get[Double]("dday")
      month <- get[Double]("month")
      hour <- get[Double]("hour")
    } yield Item(nbLead, day.toInt, month.toInt, hour.toInt)

    SQL"""
          select
            count(*) as nb,
            EXTRACT(DAY from datetime + '1 hour' ::interval) AS dday,
            EXTRACT( month from datetime + '1 hour' ::interval) AS month,
            EXTRACT( HOUR from datetime + '1 hour' ::interval) AS hour
          from lead group by hour, dday, month
       order by dday, hour
       """.as(rowParserStat *)


  }

  def leadForOneSponsor(idSponsor: Long)(implicit c: Connection): Seq[(Int, String)] = {

    val parser = for {
      nb <- get[Int]("nb")
      sponsor <- get[String]("sponsor")
    } yield (nb, sponsor)

    SQL""" select count(*) as nb, s.name as sponsor
         from SPONSOR s inner join PERSON_SPONSOR ps on s.id=ps.idsponsor
         inner join LEAD l on ps.idperson=l.idapplicant
         where s.id=${idSponsor} group by s.name
      """.as(parser.*)

  }

  def leadByhourForOneSponsor(idSponsor: Long)(implicit c: Connection): Seq[Item] = {

    val rowParserStat: RowParser[Item] = for {
      nbLead <- get[Int]("nb")
      day <- get[Double]("dday")
      month <- get[Double]("month")
      hour <- get[Double]("hour")
    } yield Item(nbLead, day.toInt, month.toInt, hour.toInt)

    SQL"""
          select
            count(*) as nb,
            EXTRACT(DAY from datetime + '1 hour'::interval) AS dday,
            EXTRACT( month from datetime + '1 hour'::interval) AS month,
            EXTRACT( HOUR from datetime + '1 hour'::interval) AS hour
          from lead l
            inner join person_sponsor ps on l.idapplicant=ps.idperson
            inner join sponsor s on ps.idsponsor=s.id where s.id=${idSponsor}
          group by hour, dday, month
          order by dday, hour
       """.as(rowParserStat *)


  }

  case class Item(nb: Int, day: Int, month: Int, hour: Int)


  object Item {
    def tupleFormated(item: Item): (Int, String) = (item.nb, s" ${item.day}-${item.month} ${item.hour}h")
  }


  def uniqPersonScanned(implicit c: Connection): Seq[Person] = {

    SQL"""select distinct p.* from lead l inner join Person p on p.id=l.idtarget;""".as(PersonDAO.rowParser.*)

  }

}
