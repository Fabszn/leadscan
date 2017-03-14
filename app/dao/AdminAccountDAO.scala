package dao

import java.sql.Connection

import anorm.{NamedParameter, RowParser, _}

import anorm.SqlParser.get
import anorm.{NamedParameter, RowParser}
import model.AdminAccount

/**
  * Created by fsznajderman on 13/03/2017.
  */
object AdminAccountDAO extends mainDBDAO[AdminAccount, Long] {


  override def rowParser: RowParser[AdminAccount] = for {
    id <- get[Option[Long]]("id")
    email <- get[String]("email_Adress")
  } yield AdminAccount(id, email)

  override def table: String = "admin_account"

  override def getParams(item: AdminAccount): Seq[NamedParameter] = Seq[NamedParameter](
    'id -> item.id,
    'email -> item.email
  )




}
