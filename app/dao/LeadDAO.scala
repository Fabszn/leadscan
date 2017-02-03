package dao

import java.sql.Connection
import java.time.LocalDateTime

import anorm.SqlParser.get
import anorm.{NamedParameter, RowParser, _}
import model.{Lead, Person}

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


}
