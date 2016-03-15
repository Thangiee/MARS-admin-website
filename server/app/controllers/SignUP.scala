package controllers

import javax.inject.Inject

import cats.std.all._
import models.Account
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import scala.concurrent.Future

class SignUp @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def page = Action { implicit request =>
    Ok(views.html.signUp(Forms.createAcc))
  }

  def create = Action.async { implicit request =>
    val invalidFormMsg = "Invalid form, fix errors and try again."

    Forms.createAcc.bindFromRequest().fold(
      formErr => Future.successful(BadRequest(views.html.signUp(formErr.withGlobalError(invalidFormMsg)))),
      data    => {
        if (data.passwd != data.passwd2) {
          val form = Forms.createAcc.fill(data).withError("passwd", "Does not match").withError("passwd2", "Does not match")
          Future.successful(BadRequest(views.html.signUp(form.withGlobalError(invalidFormMsg))))
        } else {
          Account.create(data).fold(
            err  => BadRequest(views.html.signUp(Forms.createAcc.fill(data).withGlobalError(err.msg.replace("Key", "Field")))),
            succ => Redirect(routes.Login.page()).flashing("toast" -> "Account created. Need admin approval before logging in.")
          )
        }
      }
    )
  }
}
