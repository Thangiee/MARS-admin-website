import play.api.mvc.Results.Status
import play.api.mvc._
import play.twirl.api.Html

package object controllers {

  implicit class ErrorTransformation(error: models.Error) {
    def toStatusCode: Result = Status(error.code)(error.msg)
    def toHtmlPage: Html = ???
  }
}
