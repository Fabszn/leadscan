package controllers

import batch.utils._
import model.{Person, PersonSensitive}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
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
      val convertedPerson = for {
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
        1,
        Json.toJson(kv).toString()

      )

      val convertedPersonSensitive = for {
        kv <- r
      } yield PersonSensitive(kv.get("\uFEFFRegId").map(_.toLong),
        kv.getOrElse("Email_Address", "notFound"),
        kv.getOrElse("Phone", "notFound"),
        kv.getOrElse("Company", "notFound"),
        kv.getOrElse("City", "notFound"),
        lookingForAJob = false
      )


      convertedPerson.foreach(ps.addPerson)
      convertedPersonSensitive.foreach(ps.addPersonSensitive)

    }

    Ok("import done!")
  }

  def importIndex = Action {
    Ok(views.html.importData())
  }
}


