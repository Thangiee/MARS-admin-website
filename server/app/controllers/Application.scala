package controllers

import cats.std.all._
import models.{Account, Instructor}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

class Application() extends Controller {

  def index = Action.async { implicit request =>
    Instructor.current().fold(
      err  => Redirect(routes.Login.page()).withNewSession,
      inst => {
        val data = request.session.data ++ Seq("role" -> inst.role, "full_name" -> s"${inst.firstName} ${inst.lastName}", "email" -> inst.email)
        Redirect(routes.Dashboard.page()).withSession(request.session.copy(data))
      }
    )
  }

  def logout = Action.async { implicit request =>
    Account.logout().fold(
      err  => Redirect(routes.Login.page()).withNewSession,
      succ => Redirect(routes.Login.page()).withNewSession
    )
  }
}
