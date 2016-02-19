package controllers

import models.{Assistant, Instructor, Account}
import play.api.libs.json.Json
import play.api.mvc._
import cats.std.all._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Assistants extends Controller {

  def page() = Action.async { implicit request =>
    val fetchAcc   = Account.current()
    val fetchInst  = Instructor.current()
    val fetchAssts = Assistant.all()

    // run in parallel
    val res = for {
      acc   <- fetchAcc
      inst  <- fetchInst
      assts <- fetchAssts
    } yield {
      // todo: filter out non approved assistants
      val asstsJsValue = Json.toJson(assts.map(a =>
        Map("fullName" -> s"${a.firstName} ${a.lastName}", "netId" -> a.netId, "email" -> a.email, "job" -> a.job)
      ))
      views.html.assistants(acc, inst, Json.stringify(asstsJsValue))
    }

    res.fold(
      err  => Redirect(routes.Login.page()).withNewSession,
      page => Ok(page)
    )
  }
}
