package controllers

import models.{Instructor, Account}
import play.api.mvc._
import cats.std.all._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Home extends Controller {

  def page() = Action.async { implicit request =>
    val fetchAcc  = Account.current()
    val fetchInst = Instructor.current()

    // run in parallel
    val res = for {
      acc  <- fetchAcc
      inst <- fetchInst
    } yield views.html.home(acc, inst)

    res.fold(
      err  => Redirect(routes.Login.page()).withNewSession,
      home => Ok(home)
    )
  }
}
