package dao

import java.sql.Connection

import anorm._
//import commons.utils.Anorm.UUID._

trait AnormBasicDAO[T, C <: Connection] {
  def table: String

  def getParams(item: T): Seq[NamedParameter]

  def create(item: T)(implicit conn: C): Boolean = {
    val params = getParams(item)
    val fields = params.map(_.name)
    val placeholders = fields.map(n => s"{$n}").mkString(", ")

    SQL(s"INSERT INTO $table(${fields.mkString(", ")}) VALUES ($placeholders)")
      .on(params: _*)
      .execute()
  }
}

trait AnormDAO[T, PK, C <: Connection] extends AnormBasicDAO[T, C] {
  val pkField = "id"

  def rowParser: RowParser[T]

  def customUpdatePlaceholders = Map.empty[String, (String => String)]

  def find(pk: PK)(implicit conn: C, toStmt: ToStatement[PK]): Option[T] =
    SQL"SELECT * FROM #$table WHERE #$pkField = $pk"
      .as(rowParser.singleOpt)

  def findBy[A](field: String, value: A)(implicit conn: C, toStmt: ToStatement[A]): Option[T] =
    SQL"SELECT * FROM #$table WHERE #$field = $value"
      .as(rowParser.singleOpt)

  def findAll[A](pks: Seq[PK])(implicit conn: C, toStmt: ToStatement[PK]): Seq[T] =
    if (pks.isEmpty)
      Nil
    else
      SQL"SELECT * FROM #$table WHERE #$pkField IN ($pks)"
        .as(rowParser.*)

  def exists(pk: PK)(implicit c: C, toStmt: ToStatement[PK]): Boolean =
    SQL"SELECT COUNT(*) > 0 FROM #$table WHERE #$pkField = $pk"
      .as(SqlParser.scalar[Boolean].single)

  def listBy[A](field: String, value: A)(implicit conn: C, toStmt: ToStatement[A]): Seq[T] =
    SQL"SELECT * FROM #$table WHERE #$field = $value"
      .as(rowParser.*)

  def update(item: T)(implicit conn: C, toStmt: ToStatement[PK]): Int = {
    val params = getParams(item)

    val updates = getUpdatesString(params)

    SQL(s"UPDATE $table SET $updates WHERE $pkField = {$pkField}")
      .on(params: _*)
      .executeUpdate()
  }

  def updateByNamedParameters(pk: PK)(params: List[NamedParameter])(implicit conn: C, toStmt: ToStatement[PK]): Int = {

    val updates = getUpdatesString(params)

    SQL(s"UPDATE $table SET $updates WHERE $pkField = $pk")
      .on(params: _*)
      .executeUpdate()
  }

  protected def getUpdatesString(params: Seq[NamedParameter]): String = {
    params.map(_.name).filterNot(_ == pkField).map { n =>
      val placeholder = customUpdatePlaceholders.get(n).map(_ (n)).getOrElse(s"{$n}")
      s"$n = $placeholder"
    }.mkString(", ")
  }

  def updateBy[A](pk: PK)(field: String, value: A)(implicit conn: C, toStmt: ToStatement[A], toStmtPk: ToStatement[PK]): Int =
    SQL"UPDATE #$table SET #$field = $value WHERE #$pkField = $pk"
      .executeUpdate()

  def delete(pk: PK)(implicit conn: C, toStmt: ToStatement[PK]): Boolean =
    SQL"DELETE FROM #$table WHERE #$pkField = $pk"
      .execute()

  def batchCreate(items: Seq[T])(implicit conn: C): Unit =
    items.toList match {
      case (head :: xs) => {

        val params = getParams(head)
        val fields = params.map(_.name)

        val queryFields = fields.mkString(", ")
        val queryCmd = s"INSERT INTO $table($queryFields) VALUES "

        val allPlaceholders = items.zipWithIndex.map { case (item, i) =>
          "(" + fields.map(n => s"{${n}_$i}").mkString(", ") + ")"
        }.mkString(", ")

        val allParams = items.zipWithIndex.flatMap { case (item, i) =>
          getParams(item).map(np => np.copy(name = s"${np.name}_$i"))
        }

        SQL(queryCmd + allPlaceholders)
          .on(allParams: _*)
          .execute()
      }
      case _ => () // empty list, do nothing
    }


}

trait mainDBDAO[T, PK] extends AnormDAO[T, PK, Connection]
