package mars

import cats.data.{Xor, XorT}
import cats.std.all._
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.XMLHttpRequest
import upickle.default._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object MarsApi {

  def GET(route: String)(implicit ex: ExecutionContext) = call(Ajax.get(route))

  def records(netId: String, filterOption: String): Response[Seq[Record]] =
    GET(s"/api/records/$netId/$filterOption").map(read[Seq[Record]](_))

  protected def call(request: Future[XMLHttpRequest])(implicit ex: ExecutionContext): XorF[Error, String] = {
    XorT(request.map { response =>
      response.status match {
        case 200 => Xor.Right(response.responseText)
        case code => Xor.Left(Error(code, response.responseText))
      }
    })
  }
}
