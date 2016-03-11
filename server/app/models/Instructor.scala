package models

import cats.std.all._
import play.api.libs.json._

case class Instructor(
  netId: String,
  username: String,
  role: String,
  createTime: Long,
  approve: Boolean,
  email: String,
  lastName: String,
  firstName: String
)

object Instructor {
  implicit val instFmt = Json.format[Instructor]

  def all()(implicit req: Request, ec: ExeCtx): Response[Seq[Instructor]] =
    call(GET("/instructor/all")).map(js => (js \ "instructors").as[Seq[Instructor]])

  def current()(implicit req: Request, ec: ExeCtx): Response[Instructor] =
    call(GET("/instructor")).map(_.as[Instructor])

  def changeRole(netId: String, isAdmin: Boolean)(implicit req: Request, ec: ExeCtx): Response[Account] =
    call(POST(s"/account/instructor/change-role/$netId").postForm.param("is_admin", isAdmin.toString)).map(_.as[Account])
}
