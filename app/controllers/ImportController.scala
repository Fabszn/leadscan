package controllers

import play.api.mvc.{Action, Controller}
import batch.utils._
import model.Person
import services.PersonService

/**
  * Created by fsznajderman on 07/02/2017.
  */
class ImportController(ps: PersonService) extends Controller {

  def importData() = Action(parse.multipartFormData) { implicit request =>
    val body = request.body
    body.file("csvFile").map { csvFile =>

      val csv = csvFile.ref
      val r: Seq[Map[String, String]] = loadCVSSourceFile(csv.file)

      val convertedValue = for {
        kv <- r
      } yield Person(kv.get("\uFEFFRegId").map(_.toLong),
        kv.getOrElse("first_Name", "notFound"),
        kv.getOrElse("last_Name", "notFound"),
        kv.getOrElse("gender", "_"),
        kv.getOrElse("Title", "notFound"),
        kv.getOrElse("status", "na"),
        2,
        isTraining = false,
        showSensitive = true,
        1

      )

      convertedValue.foreach(ps.addPerson)

    }

    Ok("import done!")
  }

  def importIndex = Action {
    Ok(views.html.batch.batchDemo())
  }
}
