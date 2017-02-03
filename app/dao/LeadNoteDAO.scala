package dao

import java.time.LocalDateTime

import anorm.SqlParser.get
import anorm.{NamedParameter, RowParser}
import model.LeadNote

/**
  * Created by fsznajderman on 24/01/2017.
  */
object LeadNoteDAO extends mainDBDAO[LeadNote, Long] {

  override def rowParser: RowParser[LeadNote] = for {
    id <- get[Option[Long]]("id")
    idApplicant <- get[Long]("idApplicant")
    idTarget <- get[Long]("idTarget")
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


}
