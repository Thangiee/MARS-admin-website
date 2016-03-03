package controllers

import cats.std.all._
import models.{Assistant, Instructor}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

object Approval extends Controller {

  def page = Action.async { implicit request =>
    val fetchAssts    = Assistant.all()
    val fetchInsts   = Instructor.all()

    val res = for {
      assts <- fetchAssts
      insts <- fetchInsts
    } yield views.html.approval(assts.filter(_.approve == false), insts.filter(_.approve == false))

    res.fold(
      err  => ???, // todo:
      page => Ok(page)
    )
  }
}
