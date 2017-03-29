package controllers

import java.time.LocalDateTime

import model.ErrorMessage
import play.api.mvc.Controller
import services.NotificationService
import utils.CORSAction
import utils.HateoasUtils.toHateoas
import utils.oAuthActions.ApiAuthAction

/**
  * Created by fsznajderman on 29/01/2017.
  */
class NotificationController(ns: NotificationService) extends Controller {


  def allNotif(idRecipient: String, dateTime: String) = CORSAction {
    ApiAuthAction(parse.json) {
      implicit request =>

        ns.getNotifications(idRecipient, LocalDateTime.parse(dateTime)) match {
          case Nil => NotFound(toHateoas(ErrorMessage("notifications_not_found", s"notifications are not found")))
          case notifs => Ok(toHateoas(notifs))
        }
    }
  }

  def read(id: Long) = CORSAction {
    ApiAuthAction(parse.json) {
      implicit request =>

        ns.getNotification(id) match {
          case None => NotFound(toHateoas(ErrorMessage("notification_not_found", s"notifiaction with id ${id} is not found")))
          case Some(n) => Ok(toHateoas(n))

        }


    }
  }

}
