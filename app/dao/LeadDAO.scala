package dao

import java.sql.Connection
import java.time.LocalDateTime

import anorm.SqlParser._
import anorm.{NamedParameter, RowParser, _}
import model.Lead

/**
  * Created by fsznajderman on 24/01/2017.
  */
object LeadDAO extends mainDBDAO[Lead, Long] {

  override def rowParser: RowParser[Lead] = for {
    idApplicant <- get[Long]("idApplicant")
    idTarget <- get[Long]("idTarget")
    dateTime <- get[LocalDateTime]("dateTime")
  } yield Lead(idApplicant, idTarget, dateTime)


  override def table: String = "lead"

  override def getParams(item: Lead): Seq[NamedParameter] = Seq[NamedParameter](
    'idApplicant -> item.idApplicant,
    'idTarget -> item.idTarget,
    'dateTime -> item.dateTime
  )


  def findByPks(idApplicant: Long, idTarget: Long)(implicit c: Connection): Option[Lead] =
    SQL"""
         SELECT * from LEAD where idApplicant=${idApplicant} and idTarget=$idTarget
       """.as(rowParser.singleOpt)


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
            EXTRACT(DAY from datetime) AS dday,
            EXTRACT( month from datetime) AS month,
            EXTRACT( HOUR from datetime) AS hour
          from lead group by hour, dday, month
       order by dday, hour
       """.as(rowParserStat *)


  }

  case class Item(nb: Int, day: Int, month: Int, hour: Int)


  object Item {
    def tupleFormated(item: Item): (Int, String) = (item.nb, s" ${item.day}-${item.month} ${item.hour}h")
  }

}
