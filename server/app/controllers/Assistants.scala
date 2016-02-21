package controllers

import cats.std.all._
import models.Assistant
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._

object Assistants extends Controller {

  def page() = Action.async { implicit request =>
    val res = for(assts <- Assistant.all()) yield {
      // todo: filter out non approved assistants
      val asstsJsValue = Json.toJson(assts.map(a =>
        Map("fullName" -> s"${a.firstName} ${a.lastName}", "netId" -> a.netId, "email" -> a.email, "job" -> a.job)
      ))
      views.html.assistants(Json.stringify(asstsJsValue))
    }

    res.fold(
      err  => Redirect(routes.Login.page()).withNewSession,
      page => Ok(page)
    )
  }

  def detail(netId: String) =  Action.async { implicit request =>
    Assistant.findByNetId(netId).fold(
      err  => ???, // todo: 404 page
      asst => Ok(views.html.assistantDetail(asst))
    )
  }
}
