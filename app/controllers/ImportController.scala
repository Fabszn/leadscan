package controllers

import batch.utils._
import model.{Person, PersonJson, PersonSensitive}
import play.api.libs.json.Json
import play.api.mvc.Controller
import services.{PersonService, RemoteClient}
import utils.oAuthActions.AdminAuthAction

/**
  * Created by fsznajderman on 07/02/2017.
  */
class ImportController(ps: PersonService, remote: RemoteClient) extends Controller {

  def importData() = AdminAuthAction(parse.multipartFormData) { implicit request =>
    val body = request.body
    body.file("csvFile").map { csvFile =>

      val csv = csvFile.ref
      val r: Seq[Map[String, String]] = loadCVSSourceFile(csv.file)
      val convertedPerson = for {
        kv <- r
      } yield Person(kv.get("RegId").map(_.toLong),
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
      } yield PersonSensitive(kv.get("RegId").map(_.toLong),
        kv.getOrElse("Email_Address", "notFound"),
        kv.getOrElse("Phone", "notFound"),
        kv.getOrElse("Company", "notFound"),
        kv.getOrElse("City", "notFound"),
        lookingForAJob = false
      )


      val currentToken = jsonUtils.tokenExtractorFromSession(request)
      import Person._
      convertedPerson.foreach { p =>

        //notify MyDevoxx with new person
        remote.sendPerson(Json.parse(p.json).as[PersonJson], currentToken)
        ps.addPerson(p)
      }
      convertedPersonSensitive.foreach(ps.addPersonSensitive)

    }

    Ok("import done!")
  }

  def importIndex = AdminAuthAction {
    Ok(views.html.importData())
  }
}


