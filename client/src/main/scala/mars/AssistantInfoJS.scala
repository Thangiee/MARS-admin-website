package mars

import cats.std.all._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.{Attr, ReactTagOf}
import japgolly.scalajs.react.vdom.all._
import org.scalajs.dom.document
import org.scalajs.dom.html.Div
import org.scalajs.jquery.jQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.Dynamic
import scala.scalajs.js.annotation.JSExport

@JSExport
object AssistantInfoJS {

  @JSExport
  def init(netId: String): Unit = {
    MarsApi.asstByNetId(netId).fold(
      err => toast(err.msg),
      asst => {
        renderAsstInfo(asst)
        Dynamic.global.$(".modal-trigger").leanModal()
        Dynamic.global.$("#job-select").material_select()
      }
    )
  }

  def renderAsstInfo(asst: Assistant): Unit = {

    def formField[T](_name: String, _label: String, _value: T, _type: String="text") =
      div(cls := "input-field",
        label(cls := "placeholder active", `for` := _name, _label),
        input(cls := "validate active", name := _name, id := _name, tpe := _type, defaultValue := _value.toString, step := "any")
      )

    class Backend(val $: BackendScope[Unit, Assistant]) {
      def onSubmit(asst: Assistant): Callback = {
        val List(email, empId, rate, dept, title, code, thres, job) =
          List("#email", "#emp-id", "#pay-rate", "#dept", "#title", "#code", "#thres", "input[name='job']:checked")
            .map(selector => jQuery(selector).value().toString)

        val updatedAsst = asst.copy(
          email = email, employeeId = empId, rate = rate.toDouble, department = dept,
          title = title, titleCode = code, job = job, threshold = thres.toDouble
        )

        MarsApi.updateAsst(updatedAsst).fold(
          err => toastCB("Unable to update assistant info due to " + err.msg),
          _   => { toast("Updated assistant info"); $.modState(_ => updatedAsst) }
        )
      }

      def render(asst: Assistant): ReactTagOf[Div] = {
        val isTeaching = asst.job.toLowerCase == "teaching"
        val fullName = s"${asst.firstName.capitalize} ${asst.lastName.capitalize}"

        div(cls := "card-content",
          span(cls := "card-title", b(fullName)),
          br(), br(),
          span("E-MAIL"), span(cls := "right", b(asst.email)), br(), br(),
          span("NET ID"), span(cls := "right", b(asst.netId)), br(), br(),
          span("EMPLOYEE ID"), span(cls := "right", b(asst.employeeId)), br(), br(),
          span("PAY RATE"), span(cls := "right", b(asst.rate)), br(), br(),
          span("JOB"), span(cls := "right", b(asst.job)), br(), br(),
          span("DEPARTMENT"), span(cls := "right", b(asst.department)), br(), br(),
          span("TITLE"), span(cls := "right", b(asst.title)), br(), br(),
          span("TITLE CODE"), span(cls := "right", b(asst.titleCode)), br(), br(),
          span("THRESHOLD"), span(cls := "right", b(asst.threshold)), br(), br(),

          div(cls := "card-action no-padding",
            a(id := "edit-info-btn", cls := "modal-trigger waves-effect btn-flat orange-text darken-3", href := "#edit-info-dialog", "Edit Info"),

            div(id := "edit-info-dialog", cls := "modal modal-fixed-footer",
              div(cls := "modal-content",
                h4(s"Edit $fullName Info"),
                form(
                formField(_name = "email", _label = "Email", _value = asst.email, _type = "email"),
                formField(_name = "emp-id", _label = "Employee Id", _value = asst.employeeId),
                formField(_name = "pay-rate", _label = "Pay Rate ($/Hr)", _value = asst.rate, _type = "number"),
                formField(_name = "dept", _label = "Department", _value = asst.department),
                formField(_name = "title", _label = "Title", _value = asst.title),
                formField(_name = "code", _label = "Title Code", _value = asst.titleCode),
                p(input(name := "job", tpe := "radio", id := "job1", defaultValue := "teaching", Attr("defaultChecked") := isTeaching), label(`for` := "job1", "Teaching")),
                p(input(name := "job", tpe := "radio", id := "job2", defaultValue := "grading", Attr("defaultChecked") := !isTeaching), label(`for` := "job2", "Grading")),
                div(cls := "row",
                  h6(cls := "col s3 no-padding", "Face Recognition Threshold"),
                  p(cls := "range-field col s9 tooltipped no-padding", Attr("data-position") := "top", Attr("data-tooltip") := "Lowering this value to makes it easier to clock in/out and vise versa. Default is 0.4",
                    input(tpe := "range", id:= "thres", name := "thres", min := "0", max := "1", step := "0.01", defaultValue := asst.threshold)
                  )
                )
                )
              ),
              div(cls := "modal-footer",
                button(id := "update-info-btn", href := "#!", tpe := "submit", cls := "modal-action modal-close waves-effect blue-text btn-flat",
                  onClick --> onSubmit(asst), "Update"
                )
              )
            )
          )
        )
      }
    }

    val infoCard = ReactComponentB[Unit]("info card")
      .initialState(asst)
      .renderBackend[Backend]
      .buildU

    ReactDOM.render(infoCard(), document.getElementById("info-card"))
  }
}
