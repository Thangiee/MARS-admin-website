package mars

import cats.data.{Xor, XorT}
import cats.std.all._
import org.scalajs.dom.ext.{Ajax, AjaxException}
import org.scalajs.dom.raw.XMLHttpRequest
import upickle.default._

import scala.collection.immutable.Map
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object MarsApi {

  def records(netId: String, filterOption: String): Response[Seq[Record]] =
    call(GET(s"/api/records/$netId/$filterOption")).map(read[Seq[Record]](_))

  def allAssistant: Response[Seq[Assistant]] = call(GET(s"/api/assistant/all")).map(read[Seq[Assistant]](_))

  def allInstructor: Response[Seq[Instructor]] = call(GET(s"/api/instructor/all")).map(read[Seq[Instructor]](_))

  def approveAcc(netId: String): Response[Unit] = call(POST(s"/api/account/$netId/approve")).map(_ => ())

  def deleteAcc(netId: String): Response[Unit] = call(DELETE(s"/api/account/$netId")).map(_ => ())

  private def GET(route: String)(implicit ex: ExecutionContext) = Ajax.get(route, timeout = 10000) // 10 sec timeout

  private def DELETE(route: String)(implicit ex: ExecutionContext) = Ajax.delete(route, timeout = 10000)

  private def POST(route: String, params: (String, Any)*)(implicit ex: ExecutionContext) = Ajax.post(route,
    params.map{case (k,v) => s"$k=$v"}.mkString("&"),
    headers = Map("Content-Type" -> "application/x-www-form-urlencoded"),
    timeout = 10000
  )

  private def call(request: Future[XMLHttpRequest])(implicit ex: ExecutionContext): XorF[Error, String] = {
    XorT(request
      .map(response => Xor.Right(response.responseText))
      .recover {
        case e: AjaxException => println(s"${e.xhr.statusText}: ${e.xhr.responseText}"); Xor.Left(Error(e.xhr.status, e.xhr.statusText))
        case e: Exception     => e.printStackTrace(); Xor.Left(Error(500, "unexpected error (view console log for more details)"))
      })
  }
}
