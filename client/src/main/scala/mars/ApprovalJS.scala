package mars

import japgolly.scalajs.react._
import org.scalajs.dom._
import japgolly.scalajs.react.vdom.all._

import scala.scalajs.js.annotation.JSExport
import cats.std.all._
import japgolly.scalajs.react.vdom.Attr

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.Dynamic

@JSExport
object ApprovalJS {

  case class State[T](items: Seq[T])

  @JSExport
  def init(): Unit = {

    val fetchAssts = MarsApi.allAssistant
    val fetchInsts = MarsApi.allInstructor

    (for { assts <- fetchAssts; insts <- fetchInsts } yield (assts, insts)).fold(
      err => println("error " + err),
      res => {
        val (assts, insts) = res
        renderAsstsTab(assts.filterNot(_.approve))
        renderInstsTab(insts.filterNot(_.approve))
        Dynamic.global.$(".profile").initial(Dynamic.literal(height=46, width=46, charCount=2, fontSize=20))
        Dynamic.global.$(".collapsible").collapsible()
      }
    )
  }

  private def renderAsstsTab(assts: Seq[Assistant]): Unit = {

    class HeaderBackend(val $: BackendScope[Unit, State[Assistant]]) {
      def onReject(netId: String): Callback = remove(netId)
      def onApprove(netId: String): Callback = remove(netId)
      def remove(netId: String): Callback = $.modState(s => s.copy(items = s.items.filterNot(_.netId == netId)))
    }

    val accHeader = ReactComponentB[(String, String, String, HeaderBackend)]("header")
      .render_P { case (netId , fullName, email, backend) =>
        div(cls := "collapsible-header",
          div(cls := "row",
            div(
              img(cls := "profile circle col", Attr("data-name") := fullName.split(" ").map(_.head.toString).reduce(_ + _)),
              h6(cls := "col", netId, br, fullName, br, email)
            ),
            div(cls := "secondary-content",
              a(cls := "reject-btn waves-effect waves-light btn red", onClick --> backend.onReject(netId), "Reject"),
              a(cls := "approve-btn waves-effect waves-light btn green", onClick --> backend.onApprove(netId), "Approve")
            )
          )
        )
      }
      .build

    val asstRow = ReactComponentB[(Assistant, HeaderBackend)]("content")
      .render_P { case (asst, backend) =>
        val items = Seq(
          ("Job: " + asst.job, "Pay Rate ($/Hr): $" + asst.rate.toString),
          ("Username: " + asst.username, "Department: " + asst.department),
          ("Employee Id: " + asst.employeeId, "Title: " + asst.title),
          ("Title Code: " + asst.titleCode, "")
        )

        li(
          accHeader((asst.netId, s"${asst.firstName} ${asst.lastName}", asst.email, backend)),
          div(cls := "collapsible-body",
            for ((l, r) <- items) yield {
              div(cls := "row",
                p(cls := "col s6 center-align", l),
                p(cls := "col s6 center-align", r)
              )
            }
          )
        )
      }
      .build

    val asstsList = ReactComponentB[Unit]("HelloMessage")
      .initialState(new State(assts))
      .backend(new HeaderBackend(_))
      .renderS(($, s) =>
        ul(cls := "collapsible", Attr("data-collapsible") := "accordion",
          s.items.map(asst => asstRow((asst, $.backend)))
        )
      )
      .build

    ReactDOM.render(asstsList("assts"), document.getElementById("tab1"))
  }

  private def renderInstsTab(insts: Seq[Instructor]): Unit = {

    class HeaderBackend(val $: BackendScope[Unit, State[Instructor]]) {
      def onReject(netId: String): Callback = remove(netId)
      def onApprove(netId: String): Callback = remove(netId)
      def remove(netId: String): Callback = $.modState(s => s.copy(items = s.items.filterNot(_.netId == netId)))
    }

    val accHeader = ReactComponentB[(String, String, String, HeaderBackend)]("header")
      .render_P { case (netId , fullName, email, backend) =>
        div(cls := "collapsible-header",
          div(cls := "row",
            div(
              img(cls := "profile circle col", Attr("data-name") := fullName.split(" ").map(_.head.toString).reduce(_ + _)),
              h6(cls := "col", netId, br, fullName, br, email)
            ),
            div(cls := "secondary-content",
              a(cls := "reject-btn waves-effect waves-light btn red", onClick --> backend.onReject(netId), "Reject"),
              a(cls := "approve-btn waves-effect waves-light btn green", onClick --> backend.onApprove(netId), "Approve")
            )
          )
        )
      }
      .build

    val instsList = ReactComponentB[Unit]("HelloMessage")
      .initialState(new State(insts))
      .backend(new HeaderBackend(_))
      .renderS(($, s) =>
        ul(cls := "collapsible", Attr("data-collapsible") := "accordion",
          s.items.map(inst => accHeader((inst.netId, s"${inst.firstName} ${inst.lastName}", inst.email, $.backend)))
        )
      )
      .build

    ReactDOM.render(instsList("insts"), document.getElementById("tab2"))
  }

}
