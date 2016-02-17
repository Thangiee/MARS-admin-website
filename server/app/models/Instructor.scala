package models

import cats.std.all._
import play.api.libs.json._

import scala.concurrent.ExecutionContext

case class Instructor(netId: String, email: String, lastName: String, firstName: String)

object Instructor {
  implicit val instFmt = Json.format[Instructor]

  def current()(implicit req: Request, ex: ExecutionContext): XorF[Error, Instructor] =
    call(GET("/instructor")).map(_.as[Instructor])
}
