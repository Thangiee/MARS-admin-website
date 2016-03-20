package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import cats.std.all._
import models._
import com.github.nscala_time.time.Imports._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class Home @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def page() = Action.async { implicit request =>

    val fetchAssts = Assistant.all()
    val fetchRecords = Record.all()
    val fetchPPInfo = PayPeriodInfo.current()

    val res = for {
      info    <- fetchPPInfo
      assts   <- fetchAssts.map(_.filter(_.approve))
      records <- fetchRecords.map(_.filter(r => r.outTime.isDefined && r.inTime >= info.start))
    } yield {
      val times = assts.map { asst =>
        val totalTime = records.view.filter(_.netId == asst.netId).map(r => r.outTime.get - r.inTime).sum
        TotalClockInTime(s"${asst.firstName.capitalize} ${asst.lastName.capitalize}", totalTime.toDouble / 1.hour.millis)
      }
      (info, times)
    }

    res.fold(
      err => err.toHtmlPage,
      data => Ok(views.html.home(data._1, data._2.sortBy(_.totalHrs)(Ordering[Double].reverse).take(30))) // top 30 highest total hrs
    )
  }

}
