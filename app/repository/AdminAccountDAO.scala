package repository

import java.sql.Connection

import anorm.{NamedParameter, RowParser, _}

import anorm.SqlParser.get
import anorm.{NamedParameter, RowParser}
import model.Account

/**
  * Created by fsznajderman on 13/03/2017.
  */
object AdminAccountDAO extends mainDBDAO[Account, Long] {


  override def rowParser: RowParser[Account] = for {
    id <- get[Option[Long]]("id")
    email <- get[String]("email_Adress")
  } yield Account(id, email)

  override def table: String = "admin_account"

  override def getParams(item: Account): Seq[NamedParameter] = Seq[NamedParameter](
    'id -> item.id,
    'email -> item.email
  )




}
