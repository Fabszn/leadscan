package controllers

import model.PersonJson
import org.scalatest.WordSpec
import play.api.libs.json.Json

class LeadControllerTest extends WordSpec {

  "PersonJson " should {
    "match type PersonJson" in {

      val js = Json.parse("""{"regId":"EsqdsqdRXX","firstname":"Erwin","lastname":"Morrhey","email":"erwin.morrhey@gluonhq.com","title":"-","company":"Gluon"}""").validate[PersonJson].asEither


      println(js)



    }

  }

}
