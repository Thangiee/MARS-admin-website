package models

import cats.std.all._
import play.api.libs.json.Json

case class PayPeriodInfo(start: Long, end: Long, due: Long, pay: Long)

object PayPeriodInfo {
  implicit val payPeriodFmt = Json.format[PayPeriodInfo]

  def current()(implicit req: Request, ec: ExeCtx): Response[PayPeriodInfo] =
    call(GET("/pay-period-info")).map(_.as[PayPeriodInfo])
}