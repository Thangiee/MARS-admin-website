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
    Assistant.all().fold(
      err   => Redirect(routes.Login.page()).withNewSession,
      assts => {
        // todo: filter out non approved assistants
        val asstsJsValue = Json.toJson(assts.map(a =>
          Map("fullName" -> s"${a.firstName} ${a.lastName}", "netId" -> a.netId, "email" -> a.email, "job" -> a.job)
        ))
        Ok(views.html.assistants(Json.stringify(asstsJsValue)))
      }
    )
  }

  def detail(netId: String) =  Action.async { implicit request =>
    val fetchAsst    = Assistant.findByNetId(netId)
    val fetchFaces   = Assistant.faceImgsByNetId(netId)

    val res = for {
      asst    <- fetchAsst
      faces   <- fetchFaces
    } yield {
      views.html.assistantDetail(asst, faces.map(_.url))
    }

    res.fold(
      err  => ???, // todo: 404 page
      page => Ok(page)
    )
  }
}
