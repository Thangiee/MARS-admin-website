package models

import cats.std.all._
import play.api.libs.json._

import scala.concurrent.ExecutionContext

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

case class FaceImg(id: String, url: String)

object Assistant {
  implicit val asstFmt = Json.format[Assistant]
  implicit val faceFmt = Json.format[FaceImg]

  def all()(implicit req: Request, ex: ExecutionContext): XorF[Error, Seq[Assistant]] =
    call(GET("/assistant/all")).map(js => (js \ "assistants").as[Seq[Assistant]])

  def findByNetId(id: String)(implicit req: Request, ex: ExecutionContext): XorF[Error, Assistant] =
    call(GET(s"/assistant/$id")).map(_.as[Assistant])

  def faceImgsByNetId(id: String)(implicit req: Request, ex: ExecutionContext): XorF[Error, Seq[FaceImg]] =
    call(GET(s"/face/$id")).map(js => (js \ "images").as[Seq[FaceImg]])
}