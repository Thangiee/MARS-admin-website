package mars

import cats.std.all._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all._
import org.scalajs.dom._
import org.scalajs.jquery.jQuery
import mars.components.Materialize._
import mars.components.Custom._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.Dynamic
import scala.scalajs.js.annotation.JSExport

@JSExport
object ApprovalJS {

  case class State(accs: Seq[Account])

  class Backend(val $: BackendScope[Unit, State]) {
    def onReject(netId: String, name: String)(e: ReactEventI): Callback = {
      jQuery(e.target).click() /* fix collapse bug*/
      MarsApi.deleteAcc(netId).fold(
        err => toastCB("Unable to reject account due to " + err.msg),
        _   => remove(netId, e).flatMap(_ => toastCB(s"Rejected $name"))
      )
    }

    def onApprove(netId: String, name: String)(e: ReactEventI): Callback = {
      jQuery(e.target).click()
      MarsApi.approveAcc(netId).fold(
        err => toastCB("Unable to approve account due to " + err.msg),
        _   => remove(netId, e).flatMap(_ => toastCB(s"Approved $name"))
      )
    }

    def remove(netId: String, e: ReactEventI): Callback = $.modState(s => s.copy(accs = s.accs.filterNot(_.netId == netId)))

    def renderLineItem(acc: Account) = {
      val fullName = s"${acc.firstName} ${acc.lastName}"

      val header =
        collapsibleHeader(
          accHeader(acc,
            secondaryContent(
              raisedBtn(cls := "reject-btn red", onClick ==> onReject(acc.netId, fullName), "Reject"),
              raisedBtn(cls := "approve-btn green", onClick ==> onApprove(acc.netId, fullName), "Approve")
            )
          )
        )

      acc match {
        case inst: Instructor => header
        case asst: Assistant  =>
          val items = Seq(
            ("Job: " + asst.job, "Pay Rate ($/Hr): $" + asst.rate.toString),
            ("Username: " + asst.username, "Department: " + asst.department),
            ("Employee Id: " + asst.employeeId, "Title: " + asst.title),
            ("Title Code: " + asst.titleCode, "")
          )
          li(
            header,
            collapsibleBody(
              for ((l, r) <- items) yield row(p(colm(6), centerAlign, l), p(colm(6), centerAlign, r))
            )
          )
      }
    }

    def render(s: State) = {
      collapsible(expandable, s.accs.map(renderLineItem))
    }
  }

  @JSExport
  def init(): Unit = {

    val fetchAssts = MarsApi.allAssistant
    val fetchInsts = MarsApi.allInstructor

    def renderTab(tabId: String, acc: Seq[Account]): Unit = {
      val app = ReactComponentB[Unit](acc.hashCode().toString)
        .initialState(new State(acc))
        .renderBackend[Backend]
        .buildU

      ReactDOM.render(app(), document.getElementById(tabId))
    }

    (for { assts <- fetchAssts; insts <- fetchInsts } yield (assts, insts)).fold(
      err => toast("Failed to load data due to " + err.msg),
      res => {
        val (assts, insts) = res
        renderTab("tab1", assts.filterNot(_.approve)) // render assts tab
        renderTab("tab2", insts.filterNot(_.approve)) // render insts tab
        Dynamic.global.$(".profile").initial(Dynamic.literal(height=46, width=46, charCount=2, fontSize=20))
        Dynamic.global.$(".collapsible").collapsible()
      }
    )
  }
}
