package controllers

import javax.inject.Inject

import cats.std.all._
import models.{Assistant, FaceImg}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future

class Assistants @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

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
    val fetchFaces   = FaceImg.findByNetId(netId)

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

  def update(netId: String) = Action.async { implicit request =>
    //todo:
    Forms.updateAsst.bindFromRequest().fold(
      err  => Future.successful(???),
      data => {
        println(data)
        Future.successful(Redirect(routes.Assistants.detail(netId)))
      }
    )
  }

  def deleteFace(netId: String) = Action.async { implicit request =>
    // todo:
    Forms.deleteFace.bindFromRequest().fold(
      err  => Future.successful(???),
      data => {
        println(data)
        Future.successful(Redirect(routes.Assistants.detail(netId)))
      }
    )
  }
}
