import play.api.mvc.Results.Status
import play.api.mvc._

package object controllers {

  implicit class ErrorTransformation(error: models.Error) {
    def toStatusCode: Result = Status(error.code)(error.msg)
    def toHtmlPage: Result = Status(error.code)(views.html.errorPage(s"${error.code}", error.msg))
  }
}
