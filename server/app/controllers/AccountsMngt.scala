package controllers

import javax.inject.Inject

import cats.std.all._
import models.Account
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

class AccountsMngt @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def page = Action.async { implicit request =>
    Account.current().fold(
      err => ???, // todo:
      acc => Ok(views.html.accountsMngt(acc))
    )
  }
}
