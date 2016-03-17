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

  def changePasswd(netId: String, newPass: String): Response[Unit] =
    call(POST(s"/api/account/$netId/change-pass", "new-passwd" -> newPass)).map(_ => ())

  def approveAcc(netId: String): Response[Unit] = call(POST(s"/api/account/$netId/approve")).map(_ => ())

  def deleteAcc(netId: String): Response[Unit] = call(DELETE(s"/api/account/$netId")).map(_ => ())

  def allAssistant: Response[Seq[Assistant]] = call(GET(s"/api/assistant/all")).map(read[Seq[Assistant]](_))

  def asstByNetId(id: String): Response[Assistant] = call(GET(s"/api/assistant/$id")).map(read[Assistant](_))

  def updateAsst(asst: Assistant): Response[Unit] = call(POST(s"/api/assistant/${asst.netId}/update", params =
    "email" -> asst.email, "emp-id" -> asst.employeeId, "pay-rate" -> asst.rate, "job" -> asst.job,
    "dept" -> asst.department, "title" -> asst.title, "code" -> asst.titleCode, "thres" -> asst.threshold
  )).map(_ => ())

  def faceImg(netId: String): Response[Seq[FaceImg]] = call(GET(s"/api/face-img/$netId")).map(read[Seq[FaceImg]](_))

  def deleteFaceImg(imgId: String): Response[Unit] = call(DELETE(s"/api/face-img/$imgId")).map(_ => ())

  def allInstructor: Response[Seq[Instructor]] = call(GET(s"/api/instructor/all")).map(read[Seq[Instructor]](_))

  def changeInstRole(netId: String, isAdmin: Boolean): Response[Unit] =
    call(POST(s"/api/instructor/$netId/change-role", "is-admin" -> isAdmin)).map(_ => ())

  def records(netId: String, filterOption: String): Response[Seq[Record]] =
    call(GET(s"/api/records/$netId/$filterOption")).map(read[Seq[Record]](_))

  def updateRecord(id: String, inTime: Long, inLoc: String, outTime: Long, outLoc: String): Response[Unit] =
    call(POST(s"/api/record/$id/update", "in-time" -> inTime, "in-loc" -> inLoc, "out-time" -> outTime, "out-loc" -> outLoc)).map(_ => ())

  def deleteRecord(id: String): Response[Unit] = call(DELETE(s"/api/record/$id")).map(_ => ())

  def emailTimeSheet(netId: String, year: Int, month: Int, isFirstHalf: Boolean): Response[Unit] =
    call(GET(s"/api/timesheet/$netId?y=$year&m=$month&firstHalf=$isFirstHalf")).map(_ => ())

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
