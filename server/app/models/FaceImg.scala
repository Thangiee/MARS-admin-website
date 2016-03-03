package models

import cats.std.all._
import play.api.libs.json._

import scala.concurrent.ExecutionContext

case class FaceImg(id: String, url: String)

object FaceImg {
  implicit val faceFmt = Json.format[FaceImg]

  def findByNetId(id: String)(implicit req: Request, ex: ExecutionContext): XorF[Error, Seq[FaceImg]] =
    call(GET(s"/face/$id")).map(js => (js \ "images").as[Seq[FaceImg]])
}
