package controllers

import javax.inject.Inject

import cats.std.all._
import models.Account
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import scala.concurrent.Future

class PasswdRecovery @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def forgotPasswordPage = Action { implicit request =>
    Ok(views.html.forgotPassword(Forms.emailPasswdReset))
  }

  def emailPasswordReset = Action.async { implicit request =>
    Forms.emailPasswdReset.bindFromRequest().fold(
      err   => Future.successful(BadRequest(views.html.forgotPassword(err))),
      netId =>
        Account.emailPasswdReset(netId).fold(
          err => err.code match {
            case 404 => NotFound(views.html.errorPage("404: Not Found", s"Unable to find any account with net id: '$netId'"))
            case _   => err.toHtmlPage
          },
          succ => Redirect(routes.Login.page()).flashing("toast" -> "You will receive an email with instructions for resetting your password")
        )
    )
  }

  def passwordResetPage(token: String) = Action { implicit request =>
    Ok(views.html.resetPasswd(token, Forms.changePasswd))
  }

  def resetPassword(token: String) = Action.async { implicit request =>
    Forms.changePasswd.bindFromRequest().fold(
      err     => Future.successful(BadRequest(views.html.resetPasswd(token, err))),
      newPass =>
        Account.resetPasswd(token, newPass).fold(
          err => err.code match {
            case 404 => NotFound(views.html.errorPage("Request Expired", "Try requesting a new password reset."))
            case _   => err.toHtmlPage
          },
          succ => Redirect(routes.Login.page()).flashing("toast" -> "Your password has been updated.")
        )
    )
  }

}
