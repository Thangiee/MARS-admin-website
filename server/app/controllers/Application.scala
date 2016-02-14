package controllers

import models.Account
import play.api.mvc._
import cats.std.all._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller {

  def index = Action.async { implicit request =>
    Account.current().fold(
      err => Redirect(routes.Login.page()).withNewSession,
      s   => Ok(views.html.index(s))
    )
  }
}
