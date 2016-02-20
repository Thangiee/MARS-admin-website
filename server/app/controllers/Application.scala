package controllers

import cats.std.all._
import models.{Instructor, Account}
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller {

  def index = Action.async { implicit request =>
    val fetchAcc  = Account.current()
    val fetchInst = Instructor.current()

    // run in parallel
    val res = for {
      acc  <- fetchAcc
      inst <- fetchInst
    } yield request.session.copy(request.session.data ++ Seq(
      "role" -> acc.role, "full_name" -> s"${inst.firstName} ${inst.lastName}", "email" -> inst.email
    ))

    res.fold(
      err  => Redirect(routes.Login.page()).withNewSession,
      data => Redirect(routes.Home.page()).withSession(data)
    )
  }
}
