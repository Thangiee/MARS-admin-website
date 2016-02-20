package controllers

import play.api.mvc._
import cats.std.all._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Home extends Controller {

  def page() = Action { implicit request =>
    Ok(views.html.home())
  }
}
