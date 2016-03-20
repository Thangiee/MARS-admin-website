package models

import cats.std.all._
import play.api.libs.json.Json

case class Record(
  id: Int,
  netId: String,
  inTime: Long,
  outTime: Option[Long],
  inComputerId: Option[String],
  outComputerId: Option[String]
)

object Record {
  implicit val recordFmt = Json.format[Record]

  def all()(implicit req: Request, ex: ExeCtx): Response[Seq[Record]] =
    call(GET("/records/all")).map(js => (js \ "records").as[Seq[Record]])

  def findByNetId(id: String, filter: String = "all")(implicit req: Request, ex: ExeCtx): Response[Seq[Record]] = {
    val url = filter match {
      case "pay-period" | "month" | "year" => s"/records/$id?filter=$filter"
      case _                               => s"/records/$id"
    }
    call(GET(url)).map(js => (js \ "records").as[Seq[Record]])
  }

  def update(id: String, inTime: Long, outTime: Long, inComp: String, outComp: String)(implicit req: Request, ex: ExeCtx): Response[Unit] =
    call(POST(s"/records/$id").postForm
      .params("in_time" -> inTime.toString, "out_time" -> outTime.toString, "in_comp_id" -> inComp, "out_comp_id" -> outComp))
      .map(_ => ())

  def delete(id: String)(implicit req: Request, ex: ExeCtx): Response[Unit] = call(DELETE(s"/records/$id")).map(_ => ())

  def timeSheet(netId: String, year: Int, month: Int, firstHalf: Boolean)(implicit req: Request, ex: ExeCtx): Response[Unit] =
    if (firstHalf) call(GET(s"/time-sheet/$netId/first-half-month?year=$year&month=$month")).map(_ => ())
    else           call(GET(s"/time-sheet/$netId/second-half-month?year=$year&month=$month")).map(_ => ())
}