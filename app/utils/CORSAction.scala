package utils

import play.api.mvc.{ActionBuilder, Request, Result, Results}

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by fsznajderman on 09/02/2017.
  */
object CORSAction extends ActionBuilder[Request] with Results {
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    block(request).map(r => r.withHeaders(("Access-Control-Allow-Origin", "*")))
  }
}



