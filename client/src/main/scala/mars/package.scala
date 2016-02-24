import cats.data.{Xor, XorT}
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.XMLHttpRequest

import scala.concurrent.{ExecutionContext, Future}

package object mars {
  type XorF[L, R] = XorT[Future, L, R]
  case class Error(code: Int, msg: String)

  def GET(route: String)(implicit ex: ExecutionContext) = call(Ajax.get(route))

  protected def call(request: Future[XMLHttpRequest])(implicit ex: ExecutionContext): XorF[Error, String] = {
    XorT(request.map { response =>
      response.status match {
        case 200 => Xor.Right(response.responseText)
        case code => Xor.Left(Error(code, response.responseText))
      }
    })
  }
}
