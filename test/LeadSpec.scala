import controllers.LeadController
import model.{Person, PersonSensitive}
import org.mockito.Mockito._
import org.scalatest.Matchers._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{LeadService, NotificationService, PersonService}

/**
  * Created by fsznajderman on 05/02/2017.
  */
class LeadSpec extends PlaySpec with MockitoSugar {


 /* "person without lead" should {
    "return 404 error code" in {

      val lsMock = mock[LeadService]
      when(lsMock.getLeads(1)).thenReturn(Nil)

      val lnMock = mock[NotificationService]
      val psMock = mock[PersonService]

      val leadCtr = new LeadController(lsMock, lnMock, psMock)


      val r = leadCtr.leads(1).apply(FakeRequest())


      val json = contentAsJson(r)


      json should equal(Json.parse("{\"error\":{\"code\":\"leads_not_found\",\"message\":\"Leads for person with id 1 are not found\"}}"))


    }
  }

  "person with some leads" should {
    "return 200 with json" in {

      val lsMock = mock[LeadService]
      val psMock = mock[PersonService]
      val sens = Some(PersonSensitive(None, "f@f.fr", "000", "company", "here", false))
      when(lsMock.getLeads(1)).thenReturn(Seq(Person(Some(1), "pierre", "Paul", "M", "Contractor", "test", 16, true, false, sens, 1)))

      val lnMock = mock[NotificationService]

      val leadCtr = new LeadController(lsMock, lnMock, psMock)


      val r = leadCtr.leads(1).apply(FakeRequest())


      val json = contentAsJson(r)

      println(json)
      json should equal(Json.parse("{\"error\":{\"code\":\"leads_not_found\",\"message\":\"Leads for person with id 1 are not found\"}}"))


    }
  }*/


}
