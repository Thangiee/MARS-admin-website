import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent._

class ErrorHandler extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(
      statusCode match {
        case 404  => NotFound(views.html.errorPage(s"$statusCode: Page not found", "The page you requested does not exist"))
        case code => Status(statusCode)(views.html.errorPage(s"$statusCode", message))
      }
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Future.successful(
      InternalServerError(views.html.errorPage("A server error occurred", exception.getMessage))
    )
  }
}