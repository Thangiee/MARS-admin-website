package controllers

import javax.inject.Inject

import cats.std.all._
import models.Account
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import scala.concurrent.Future

class Login @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def page = Action { implicit request =>
    Ok(views.html.login(Forms.login))
  }

  def auth = Action.async { implicit request =>
    Forms.login.bindFromRequest().fold(
      formErr  => { Future.successful(BadRequest(views.html.login(formErr))) },
      formData => {
        val (user, passwd) = formData
        Account.authenticate(user, passwd).fold(
          error => Status(error.code)(views.html.login(Forms.login.fill(formData).withError("auth-err", error.msg))),
          succ  => {
            val (cookies, acc) = succ
            if (acc.role == "admin" || acc.role == "instructor")
              Redirect(routes.Application.index()).withSession("authn_cookies" -> cookies.mkString("; "))
            else
              Forbidden(views.html.login(Forms.login.withError("auth-err", s"${acc.role} cannot use the website.")))
          }
        )
      }
    )
  }
}
