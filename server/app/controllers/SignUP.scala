package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

class SignUp @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def page = Action { implicit request =>
    Ok(views.html.signUp(Forms.createAcc))
  }

  def create = Action { implicit request =>
    val invalidFormMsg = "Invalid form, fix errors and try again."

    Forms.createAcc.bindFromRequest().fold(
      formErr => BadRequest(views.html.signUp(formErr.withGlobalError(invalidFormMsg))),
      data    => {
        if (data.passwd != data.passwd2) {
          val form = Forms.createAcc.fill(data).withError("passwd", "Does not match").withError("passwd2", "Does not match")
          BadRequest(views.html.signUp(form.withGlobalError(invalidFormMsg)))
        } else {
          // todo:
          Redirect(routes.Login.page())
        }
      }
    )
  }

}
