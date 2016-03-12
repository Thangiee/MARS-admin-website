package controllers

import javax.inject.Inject

import cats.std.all._
import models.{Assistant, Instructor}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import scala.concurrent.Future

class Approval @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def page = Action.async { implicit request =>
    Future.successful(Ok(views.html.approval()))
  }
}
