package mars

import cats.data.{Xor, XorT}
import cats.std.all._
import org.scalajs.dom.ext.{Ajax, AjaxException}
import org.scalajs.dom.raw.XMLHttpRequest
import upickle.default._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object MarsApi {

  def GET(route: String)(implicit ex: ExecutionContext) = Ajax.get(route, timeout = 10000) // 10 sec timeout
  def POST(route: String, params: Map[String, String] = Map.empty)(implicit ex: ExecutionContext) = Ajax.post(route, write(params), timeout = 10000)
  def DELETE(route: String)(implicit ex: ExecutionContext) = Ajax.delete(route, timeout = 10000)

  def records(netId: String, filterOption: String): Response[Seq[Record]] =
    call(GET(s"/api/records/$netId/$filterOption")).map(read[Seq[Record]](_))

  def allAssistant: Response[Seq[Assistant]] = call(GET(s"/api/assistant/all")).map(read[Seq[Assistant]](_))

  def allInstructor: Response[Seq[Instructor]] = call(GET(s"/api/instructor/all")).map(read[Seq[Instructor]](_))

  def approveAcc(netId: String): Response[Unit] = call(POST(s"/api/account/$netId/approve")).map(_ => ())

  def deleteAcc(netId: String): Response[Unit] = call(DELETE(s"/api/account/$netId")).map(_ => ())

  protected def call(request: Future[XMLHttpRequest])(implicit ex: ExecutionContext): XorF[Error, String] = {
    XorT(request.map(response =>
      response.status match {
        case 200  => Xor.Right(response.responseText)
        case code => Xor.Left(Error(code, s"$code: ${response.responseText}"))
      }
    ) recover {
      case e: AjaxException => e.printStackTrace(); Xor.Left(Error(404, "unable to connect to the server"))
      case e: Exception     => e.printStackTrace(); Xor.Left(Error(500, "Internal Service Error"))
    })
  }
}
