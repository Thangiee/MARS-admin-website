package models

import cats.std.all._
import play.api.libs.json._

case class Assistant(
  netId: String,
  username: String,
  role: String,
  createTime: Long,
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

object Assistant {
  implicit val asstFmt = Json.format[Assistant]

  def all()(implicit req: Request, ec: ExeCtx): Response[Seq[Assistant]] =
    call(GET("/assistant/all")).map(js => (js \ "assistants").as[Seq[Assistant]])

  def findByNetId(id: String)(implicit req: Request, ec: ExeCtx): Response[Assistant] =
    call(GET(s"/assistant/$id")).map(_.as[Assistant])

  def update(netId: String, empId: String, rate: Double, job: String, dept: String, title: String, code: String, thres: Double)(implicit req: Request, exeCtx: ExeCtx): Response[Unit] =
    call(POST(s"/assistant/$netId").postForm
      .params("emp_id" -> empId, "rate" -> rate.toString, "job" -> job, "dept" -> dept, "title" -> title, "title_code" -> code, "threshold" -> thres.toString))
      .map(_ => ())
}