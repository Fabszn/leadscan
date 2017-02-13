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

    SQL"""select * from SPONSOR""".as(rowParser.*)

  }
}
