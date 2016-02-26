package controllers

import cats.std.all._
import com.github.nscala_time.time.Imports._
import models.Record
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._

object Api extends Controller {

  def records(netId: String, filter: String) = Action.async { implicit request =>
    Record.findByNetId(netId, filter).fold(
      err     => err.toResponse,
      records =>
        Ok(Json.toJson(records.map(r => Map(
          "id"      -> r.id.toString,
          "netId"   -> r.netId,
          "inTime"  -> formatTime(r.inTime),
          "inLoc"   -> r.inComputerId.getOrElse(""),
          "outTime" -> r.outTime.map(formatTime).getOrElse(""),
          "outLoc"  -> r.outComputerId.getOrElse("")
        ))))
    )
  }

  private def formatTime(ms: Long): String = DateTimeFormat.forPattern("MM/dd/YY h:mm a").print(ms)
}