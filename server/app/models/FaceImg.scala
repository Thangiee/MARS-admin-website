package models

import cats.std.all._
import play.api.libs.json._

case class FaceImg(id: String, url: String)

object FaceImg {
  implicit val faceFmt = Json.format[FaceImg]

  def findByNetId(id: String)(implicit req: Request, ec: ExeCtx): Response[Seq[FaceImg]] =
    call(GET(s"/face/$id")).map(js => (js \ "images").as[Seq[FaceImg]])

  def delete(imgId: String)(implicit req: Request, ec: ExeCtx): Response[Unit] =
    call(DELETE(s"/face/$imgId")).map(_ => ())
}
