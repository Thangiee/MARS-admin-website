package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import cats.std.all._
import models.PayPeriodInfo
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class Home @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def page() = Action.async { implicit request =>
    PayPeriodInfo.current().fold(
      err => err.toHtmlPage,
      info => Ok(views.html.home(info))
    )
  }

}
