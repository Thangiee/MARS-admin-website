package controllers

import javax.inject.Inject

import models.Account
import cats.std.all._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class Login @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val loginForm = Form(
    tuple(
      "login-user" -> nonEmptyText.verifying("Can't have space", !_.contains(" ")),
      "login-passwd" -> nonEmptyText
    )
  )

  def page = Action {
    Ok(views.html.login(loginForm))
  }

  def auth = Action.async { implicit request =>
   loginForm.bindFromRequest().fold(
      formErr  => { Future.successful(BadRequest(views.html.login(formErr))) },
      formData => {
        val (user, passwd) = formData
        Account.authenticate(user, passwd).fold(
          error => Status(error.code)(views.html.login(loginForm.withError("auth-err", error.msg))),
          succ  => {
            val (cookies, acc) = succ
            if (acc.role == "admin" || acc.role == "instructor")
              Redirect(routes.Application.index()).withSession("authn_cookies" -> cookies.mkString("; "))
            else
              Forbidden(views.html.login(loginForm.withError("auth-err", s"${acc.role} cannot use the website.")))
          }
        )
      }
    )
  }
}
