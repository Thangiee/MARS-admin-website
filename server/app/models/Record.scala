package models

import cats.std.all._
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

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

  def findByNetId(id: String, filter: String = "all")(implicit req: Request, ex: ExeCtx): Response[Seq[Record]] = {
    val url = filter match {
      case "pay-period" | "month" | "year" => s"/records/$id?filter=$filter"
      case _                               => s"/records/$id"
    }
    call(GET(url)).map(js => (js \ "records").as[Seq[Record]])
  }
}