package controllers

import cats.std.all._
import models.Assistant
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import play.api.i18n.Messages.Implicits._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future

object Assistants extends Controller {

  case class UpdateInfoForm(email: String, empId: String, payRate: Double, job: String, dept: String, title: String, code: String, thres: Double)
  val updateInfoForm: Form[UpdateInfoForm] = Form(
    mapping(
      "email"    -> email,
      "emp-id"   -> nonEmptyText,
      "pay-rate" -> of[Double],
      "job"      -> nonEmptyText,
      "dept"     -> nonEmptyText,
      "title"    -> nonEmptyText,
      "code"     -> nonEmptyText,
      "thres"    -> of[Double].verifying(min(0.0), max(1.0))
    )(UpdateInfoForm.apply)(UpdateInfoForm.unapply)
  )

  val deleteFaceForm = Form("face-id" -> nonEmptyText)

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

  def update(netId: String) = Action.async { implicit request =>
    //todo:
    updateInfoForm.bindFromRequest().fold(
      err  => Future.successful(???),
      data => {
        println(data)
        Future.successful(Redirect(routes.Assistants.detail(netId)))
      }
    )
  }

  def deleteFace(netId: String) = Action.async { implicit request =>
    // todo:
    deleteFaceForm.bindFromRequest().fold(
      err  => Future.successful(???),
      data => {
        println(data)
        Future.successful(Redirect(routes.Assistants.detail(netId)))
      }
    )
  }
}
