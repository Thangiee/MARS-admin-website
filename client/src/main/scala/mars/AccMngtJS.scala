package mars

import cats.std.all._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.{Attr, ReactTagOf}
import japgolly.scalajs.react.vdom.all._
import org.scalajs.dom.document
import org.scalajs.jquery.jQuery
import org.scalajs.dom.html.Div

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.Dynamic
import scala.scalajs.js.annotation.JSExport

@JSExport
object AccMngtJS {

  case class State(assts: Seq[Assistant], insts: Seq[Instructor], admins: Seq[Instructor])

  class Backend(val $: BackendScope[Unit, State]) {

    def handleKeyUp(netId: String)(e: ReactEventI): Callback = {
      if (e.target.value == netId) jQuery(s"#delete-btn-$netId").removeClass("disabled")
      else                         jQuery(s"#delete-btn-$netId").addClass("disabled")

      Callback.empty
    }

    def handleDeleteAcc(netId: String)(e: ReactEventI): Callback = {
      MarsApi.deleteAcc(netId).fold(
        err  => toastCB("Unable to delete account due to " + err.msg),
        succ => {
          if (!jQuery(e.target).hasClass("disabled")) {
            jQuery("input[name='delete']").value("") // fix bug: input propagate to next item after
            jQuery(e.target).addClass("disabled")    //          deleting current item
            $.modState(s => s.copy(s.assts.filterNot(_.netId == netId),
                                   s.insts.filterNot(_.netId == netId),
                                   s.admins.filterNot(_.netId == netId))).map(_ => toast("Account deleted"))
          } else {
            Callback.empty
          }
        }
      )
    }

    def handleChangeRole(acc: Instructor)(e: ReactEventI): Callback = {
      val isAdmin = jQuery(e.target).is(":checked")
      val msg = if (isAdmin) "promoted to admin role" else "demoted from admin role"

      MarsApi.changeInstRole(acc.netId, isAdmin).fold(
        err  => toastCB("Unable to change role due to " + err.msg),
        succ => toastCB(s"${acc.firstName} ${acc.lastName} has been $msg")
      )
    }

    def header(netId: String, fullName: String, email: String): ReactTagOf[Div] = {
      div(cls := "collapsible-header",
        div(cls := "row",
          div(
            img(cls := "profile circle col", Attr("data-name") := fullName.split(" ").map(_.head.toString).reduce(_ + _)),
            h6(cls := "col", s"Net Id: $netId", br(), fullName, br(), email)
          )
        )
      )
    }

    def instContent(inst: Instructor): ReactTagOf[Div] = {
      content(
        inst.netId,
        div(cls := "switch",
          label(
            "Instructor",
            input(`type` := "checkbox", Attr("defaultChecked") := (inst.role == "admin"), onClick ==> handleChangeRole(inst)),
            span(cls := "lever"),
            "Admin"
          )
        )
      )
    }

    def asstsContent(asst: Assistant): ReactTagOf[Div] = {
      val items = Seq(
        ("Job: " + asst.job, "Pay Rate ($/Hr): $" + asst.rate.toString),
        ("Username: " + asst.username, "Department: " + asst.department),
        ("Employee Id: " + asst.employeeId, "Title: " + asst.title),
        ("Title Code: " + asst.titleCode, "")
      ).map { case (l, r) =>
        div(cls := "row",
          p(cls := "col s6 center-align", l),
          p(cls := "col s6 center-align", r)
        )
      }

      content(asst.netId, div(items))
    }

    def content(netId: String, tag: ReactTag): ReactTagOf[Div] = {
      div(cls := "collapsible-body",
        div(id := "action-container",
          tag,
          div(cls := "divider"),
          form(action := "#",
            div(cls := "file-field input-field",
              a(id := s"delete-btn-$netId", cls := "btn red disabled", "Delete Account", onClick ==> handleDeleteAcc(netId)),
              div(cls := "file-path-wrapper",
                input(name := "delete", tpe := "text", placeholder := "Type in this account net id to confirm.", onKeyUp ==> handleKeyUp(netId))
              )
            )
          )
        )
      )
    }

    def render(s: State): ReactTagOf[Div] = {
      div(cls := "row",
        div(cls := "col s12 ",
          ul(cls := "tabs",
            li(cls := "tab col s4", a(href := "#tab1", "Assistants")),
            li(cls := "tab col s4", a(href := "#tab2", "Instructors")),
            li(cls := "tab col s4", a(href := "#tab3", "Admins"))
          )
        ),

        div(id := "tab1", cls := "col s12",
          ul(cls := "collapsible", Attr("data-collapsible") := "accordion",
            s.assts.map(asst => li(
              header(asst.netId, s"${asst.firstName} ${asst.lastName}", asst.email),
              asstsContent(asst)
            ))
          )
        ),

        div(id := "tab2", cls := "col s12",
          ul(cls := "collapsible", Attr("data-collapsible") := "accordion",
            s.insts.map(inst => li(
              header(inst.netId, s"${inst.firstName} ${inst.lastName}", inst.email),
              instContent(inst)
            ))
          )
        ),

        div(id := "tab3", cls := "col s12",
          ul(cls := "collapsible", Attr("data-collapsible") := "accordion",
            s.admins.map(admin => li(
              header(admin.netId, s"${admin.firstName} ${admin.lastName}", admin.email),
              instContent(admin)
            ))
          )
        )
      )
    }
  }

  @JSExport
  def init(thisAccNetId: String): Unit = {
    val fetchAssts = MarsApi.allAssistant
    val fetchInsts = MarsApi.allInstructor

    val res = for {
      assts    <- fetchAssts.map(_.filter(_.approve))
      allInsts <- fetchInsts.map(_.filter(acc => acc.approve && acc.netId != thisAccNetId))  // also remove current acc from result
      (admins, insts) = allInsts.partition(_.role.toLowerCase == "admin")
    } yield (assts, insts, admins)

    res.fold(
      err => toast("Failed to load data due to " + err.msg),
      res => {
        val (assts, insts, admins) = res

        val app = ReactComponentB[Unit]("acc mngt")
          .initialState(State(assts, insts, admins))
          .renderBackend[Backend]
          .build

        ReactDOM.render(app(""), document.getElementById("react"))
        Dynamic.global.$(".profile").initial(Dynamic.literal(height=46, width=46, charCount=2, fontSize=20))
        Dynamic.global.$(".collapsible").collapsible()
        Dynamic.global.$("ul.tabs").tabs()
      }
    )
  }
}