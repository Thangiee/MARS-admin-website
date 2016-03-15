import cats.data.XorT
import japgolly.scalajs.react.Callback

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.scalajs.js.Dynamic

package object mars {
  type Response[R] = XorF[Error, R]
  type XorF[L, R] = XorT[Future, L, R]
  case class Error(code: Int, msg: String)

  def toastCB(text: String, duration: Int = 7000): Callback = Callback(Dynamic.global.Materialize.toast(text, duration))

  def toast(text: String, duration: Int = 7000): Unit = Dynamic.global.Materialize.toast(text, duration)

  implicit def FutureToCallback[A](f: Future[Callback])(implicit ec: ExecutionContext): Callback = Callback.future(f)

  case class Record(id: String, netId: String, inTime: String, outTime: String, inLoc: String, outLoc: String)

  case class FaceImg(id: String, url: String)

  case class Assistant(
    netId: String,
    username: String,
    role: String,
    createTime: Double,
    approve: Boolean,
    rate: Double,
    email: String,
    job: String,
    department: String,
    lastName: String,
    firstName: String,
    employeeId: String,
    title: String,
    titleCode: String,
    threshold: Double
  )

  case class Instructor(
    netId: String,
    username: String,
    role: String,
    createTime: Double,
    approve: Boolean,
    email: String,
    lastName: String,
    firstName: String
  )
}
