package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import cats.std.all._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class Home @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def page() = Action { implicit request =>
    Ok(views.html.home())
  }
}
