package controllers

import play.api.mvc._

object Application extends Controller {

  def index = Action { implicit request =>
    Redirect(routes.Home.page())
  }
}
