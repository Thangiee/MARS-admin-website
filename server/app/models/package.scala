import java.net.HttpCookie

import cats.data.{Xor, XorT}
import play.api.libs.json._
import play.api.mvc.AnyContent

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scalaj.http._
import scala.collection.JavaConversions._

package object models {

  type XorF[L, R] = XorT[Future, L, R]
  type Request = play.api.mvc.Request[AnyContent]

  case class Error(code: Int, msg: String)

  protected[models] val baseUrl = "http://52.33.35.165:8080/api"
  protected[models] def POST(route: String) = Http(baseUrl + route).timeout(10000, 10000).method("POST")
  protected[models] def GET(route: String)  = Http(baseUrl + route).timeout(10000, 10000).method("GET")

  // todo: need caching?
  protected[models] def call(request: HttpRequest)(implicit req: Request, ex: ExecutionContext): XorF[Error, JsValue] = {
    XorT(Future {
      val authnCookies = Try(HttpCookie.parse(req.session.get("authn_cookies").getOrElse("")).toList).getOrElse(Nil)
      Try(request.cookies(authnCookies).asString) match {
        case Success(HttpResponse(body, 200, _))  => Xor.Right(Try(Json.parse(body)).getOrElse(JsNull))
        case Success(HttpResponse(body, code, _)) => Xor.Left(Error(code, body))
        case Failure(e)      => e.printStackTrace(); Xor.Left(Error(500, "Internal server error."))
      }
    })
  }
}
