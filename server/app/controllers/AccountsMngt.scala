package controllers

import javax.inject.Inject

import cats.std.all._
import models.{Assistant, Instructor}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

class AccountsMngt @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def page = Action.async { implicit request =>
    val fetchAssts    = Assistant.all()
    val fetchInsts   = Instructor.all()

    val res = for {
      assts <- fetchAssts
      insts <- fetchInsts
      asstsFiltered  = assts.filter(_.approve)
      instsFiltered  = insts.filter(x => x.approve && x.role == "instructor")
      adminsFiltered = insts.filter(_.role == "admin")
    } yield views.html.accountsMngt(asstsFiltered, instsFiltered, adminsFiltered)

    res.fold(
      err  => ???, // todo:
      page => Ok(page)
    )
  }
}
