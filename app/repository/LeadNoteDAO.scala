package repository

import java.sql.Connection
import java.time.LocalDateTime

import anorm.SqlParser.get
import anorm.{NamedParameter, RowParser, _}
import model.LeadNote


/**
  * Created by fsznajderman on 24/01/2017.
  */
object LeadNoteDAO extends mainDBDAO[LeadNote, Long] {

  override def rowParser: RowParser[LeadNote] = for {
    id <- get[Option[Long]]("id")
    idApplicant <- get[String]("idApplicant")
    idTarget <- get[String]("idTarget")
    note <- get[String]("note")
    notedateTime <- get[LocalDateTime]("dateTime")
  } yield LeadNote(id, idApplicant, idTarget, note, notedateTime)

  override def table: String = "lead_note"

  override def getParams(item: LeadNote): Seq[NamedParameter] = Seq[NamedParameter](
    'idApplicant -> item.idApplicant,
    'idTarget -> item.idTarget,
    'note -> item.note,
    'dateTime -> item.dateTime
  )


  def findNoteByApplicantAndTarget(idApplicatn: String, idTarget: String)(implicit c :Connection): Seq[LeadNote] = {

    SQL"""select * from lead_note where idapplicant = $idApplicatn and idtarget=$idTarget""".as(rowParser.*)


  }


  def updateNote(note: LeadNote)(implicit c: Connection): Unit = {

    SQL"""update lead_note set note=${note.note} where idapplicant = ${note.idApplicant} and idtarget=${note.idTarget}""".executeUpdate()

  }


}
