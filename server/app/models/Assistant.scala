package models

import cats.std.all._
import play.api.libs.json._

import scala.concurrent.ExecutionContext

case class Assistant(netId: String, rate: Double, email: String, job: String, department: String,
  lastName: String, firstName: String, employeeId: String, title: String, titleCode: String, threshold: Double)


object Assistant {
  implicit val asstFmt = Json.format[Assistant]

  def all()(implicit req: Request, ex: ExecutionContext): XorF[Error, Seq[Assistant]] =
    call(GET("/assistant/all")).map(js => (js \ "assistants").as[Seq[Assistant]])

}