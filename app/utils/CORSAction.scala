package utils

import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by fsznajderman on 09/02/2017.
  */
case class CORSAction[A](action: Action[A]) extends Action[A] with Results {


  def apply(request: Request[A]): Future[Result] = {

    action(request).map(r => r.withHeaders(("Access-Control-Allow-Origin", "*")))
  }

  override def parser: BodyParser[A] = action.parser
}



