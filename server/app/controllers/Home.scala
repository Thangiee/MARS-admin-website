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

    val fetchToken = webSocketToken()
    val fetchAssts = Assistant.all()
    val fetchRecords = Record.all()
    val fetchPPInfo = PayPeriodInfo.current()

    val res = for {
      token   <- fetchToken
      info    <- fetchPPInfo
      assts   <- fetchAssts.map(_.filter(_.approve))
      records <- fetchRecords.map(_.filter(r => r.outTime.isDefined && r.inTime >= info.start))
    } yield {
      val times = assts.map { asst =>
        val totalTime = records.view.filter(_.netId == asst.netId).map(r => r.outTime.get - r.inTime).sum
        TotalClockInTime(s"${asst.firstName.capitalize} ${asst.lastName.capitalize}", totalTime.toDouble / 1.hour.millis)
      }
      (token, info, times)
    }

    res.fold(
      err => {
        println(err)
        err.toHtmlPage
      },
      data => {
        val (token, info, asstsTime) = data
        val topAsstsTimes = asstsTime.sortBy(_.totalHrs)(Ordering[Double].reverse).take(30) // top 30 highest total hrs
        Ok(views.html.home(token, info, topAsstsTimes))
      }
    )
  }

}
