package controllers

import javax.inject.Inject

import controllers.SignUp.CreateAccForm
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

class SignUp @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val createAccForm = Form(
    mapping(
      "netid"      -> nonEmptyText,
      "username"   -> nonEmptyText,
      "passwd"     -> nonEmptyText(minLength = 6),
      "passwd2"    -> nonEmptyText(minLength = 6),
      "email"      -> email,
      "last-name"  -> nonEmptyText,
      "first-name" -> nonEmptyText
    )(CreateAccForm.apply)(CreateAccForm.unapply)
  )

  def page = Action { implicit request =>
    Ok(views.html.signUp(createAccForm))
  }

  def create = Action { implicit request =>
    val invalidFormMsg = "Invalid form, fix errors and try again."

    createAccForm.bindFromRequest().fold(
      formErr => BadRequest(views.html.signUp(formErr.withGlobalError(invalidFormMsg))),
      data    => {
        if (data.passwd != data.passwd2) {
          val form = createAccForm.fill(data).withError("passwd", "Does not match").withError("passwd2", "Does not match")
          BadRequest(views.html.signUp(form.withGlobalError(invalidFormMsg)))
        } else {
          // todo:
          Redirect(routes.Login.page())
        }
      }
    )
  }
}

object SignUp {
  case class CreateAccForm(netId: String, username: String, passwd: String, passwd2: String, email: String, lastName: String, firstName: String)
}