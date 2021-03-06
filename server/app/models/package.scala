import java.net.HttpCookie

import cats.data.{Xor, XorT}
import cats.std.all._
import com.typesafe.config.ConfigFactory
import play.api.libs.json._
import play.api.mvc.AnyContent

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scalaj.http._

package object models {

  type ExeCtx = scala.concurrent.ExecutionContext
  type XorF[L, R] = XorT[Future, L, R]
  type Request = play.api.mvc.Request[AnyContent]
  type Response[R] = XorF[Error, R]

  case class Error(code: Int, msg: String)

  def webSocketToken()(implicit req: Request, ex: ExeCtx): Response[String] =
    call(GET("/web-socket-token")).map(js => (js \ "token").as[String])

  private val baseUrl = ConfigFactory.load().getString("backend.url") + "/api"
  private def httpMethod(route: String, method: String) = Http(baseUrl + route).timeout(10000, 10000).method(method)
  protected[models] def GET(route: String)  = httpMethod(route, "GET")
  protected[models] def POST(route: String) = httpMethod(route, "POST")
  protected[models] def PUT(route: String)  = httpMethod(route, "PUT")
  protected[models] def DELETE(route: String) = httpMethod(route, "DELETE")

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
