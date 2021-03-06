package mars

import cats.std.all._
import org.scalajs.dom.{Event, document}
import org.scalajs.jquery._

import scala.language.postfixOps
import scala.scalajs.js.Dynamic
import scala.scalajs.js.annotation.JSExport
import scalatags.Text.TypedTag
import scalatags.Text.all._
import scala.concurrent.ExecutionContext.Implicits.global

@JSExport
object RecordsTableJS {

  private val $ = jQuery
  private val timeFormat = "MM/DD/YY h:mm a"

  @JSExport
  def init(netId: String): Unit = {
    // initial render of the table when page loads
    renderTable(netId, "pay-period")

    // filter btn
    $(document).on("click", "#filter", (e: Event) => {
      val filter = $(e.target).data("value").toString
      renderTable(netId, filter)
    })

    // edit icon
    $(document).on("click", "#edit-record-icon", (e: Event) => {
      val id = $(e.target).closest("a").data("id")
      Dynamic.global.$("#edit-record-dialog-"+id).openModal() // show edit dialog
    })

    // update btn in edit dialog
    $(document).on("click", "#update-record-btn", (e: Event) => {
      val id = $(e.target).data("id").toString
      val inTime  = $("#in-time-input-"+id).`val`().toString
      val outTime = $("#out-time-input-"+id).`val`().toString
      val inLoc   = $("#in-loc-input-"+id).`val`().toString
      val outLoc  = $("#out-loc-input-"+id).`val`().toString

      val moment = Dynamic.global.moment
      val inMillis  = moment(inTime, timeFormat).valueOf().toString.toLong
      val outMillis = moment(outTime, timeFormat).valueOf().toString.toLong

      MarsApi.updateRecord(id, inMillis, inLoc, outMillis, outLoc).fold(
        err  => toast(s"Unable to update record due to ${err.msg}"),
        succ => {
          val filter = $("#current-filter").data("value").toString
          renderTable(netId, filter)
          toast("Record updated")
        }
      )
    })

    // delete icon
    $(document).on("click", "#del-record-icon", (e: Event) => {
      val id = $(e.target).closest("a").data("id")
      Dynamic.global.$("#delete-record-dialog-"+id).openModal() //show delete confirmation dialog
    })

    // delete btn in delete dialog
    $(document).on("click", "#delete-record-btn", (e: Event) => {
      val id = $(e.target).data("id").toString
      MarsApi.deleteRecord(id).fold(
        err  => toast(s"Unable to delete record due to ${err.msg}"),
        succ => {
          Dynamic.global.$(e.target).closest("a").tooltip("remove")
          $(e.target).closest("tr").empty()
          toast("Record deleted")
        }
      )
    })

    // email icon
    $(document).on("click", "#email-icon", (e: Event) => {
      Dynamic.global.$("#email-dialog").openModal()
    })

    // send time sheet btn
    $(document).on("click", "#ts-send-btn", (e: Event) => {
      val year  = $("select[name=ts-year]").`val`().toString.toInt
      val month = $("select[name=ts-month]").`val`().toString.toInt
      val isFirstHalf = $("select[name=ts-half-month]").`val`().toString.toBoolean

      MarsApi.emailTimeSheet(netId, year, month, isFirstHalf).fold(
        err  => toast(s"Unable to send time sheet due to ${err.msg}"),
        succ => toast("Time sheet sent. It may take up to a few minutes to arrive.")
      )
    })
  }

  private def renderTable(netId: String, filterOption: String): Unit = {
    MarsApi.records(netId, filterOption).map { records =>
      val completeRecord = records.filter(_.outTime.nonEmpty) // only show records that have been clocked out
      $("#record-table-container").empty().append(recordsTable(completeRecord, filterOption).render)

      // init Js libraries stuff
      Dynamic.global.$(".dropdown-button").dropdown()
      $(document).ready(Dynamic.global.$(".tooltipped").tooltip())
      Dynamic.global.$(".date-time-picker").bootstrapMaterialDatePicker(Dynamic.literal(format = timeFormat, shortTime = true))
      Dynamic.global.$("select[name^=ts]").material_select()
    }
  }

  private def recordsTable(records: Seq[Record], currentFilter: String): TypedTag[String] = {
    div(`class`:="card",
      div(`class`:="card-content",
        span(`class`:="card-title",
          b("Clock In/Out Records"),
          a(id:="current-filter", `class`:="dropdown-button btn-flat right", href:="#", data("activates"):="dropdown1", data.value:=currentFilter,
            currentFilter,
            i(`class`:="material-icons right", "filter_list")
          ),
          ul(id:="dropdown1", `class`:="dropdown-content",
            li(a( id:="filter", data.value:="pay-period", href:="#!","pay-period")),
            li(a( id:="filter", data.value:="month", href:="#!","month")),
            li(a( id:="filter", data.value:="year", href:="#!","year")),
            li(a( id:="filter", data.value:="all", href:="#!","all"))
          ),
          a(id:="email-icon", `class`:="btn-flat right tooltipped", href:="#", data.position := "left", data.delay := "50", data.tooltip := "E-mail Time Sheet",
            i(`class`:="material-icons", "email")
          ),
          emailTimeSheetDialog()
        ),

        table(`class`:="striped responsive-table",
          thead(
            tr(
              th(data.field:="in-time","In Time"),
              th(data.field:="in-id","In Location"),
              th(data.field:="out-time","Out Time"),
              th(data.field:="out-id", "Out Location"),
              th(data.field:="action")
            )
          ),
          tbody(
            records.map(r => {
              tr(id := "record-" + r.id,
                td(r.inTime), td(r.inLoc), td(r.outTime), td(r.outLoc),
                td(
                  a(id := "edit-record-icon", cls := "tooltipped", data.position := "left", data.delay := "50", data.tooltip := "edit", data.id := r.id, href := "#",
                    i(cls := "material-icons", "edit")
                  ),
                  editRecordDialog(r),
                  a(id := "del-record-icon", cls := "tooltipped", data.position := "right", data.delay := "50", data.tooltip := "delete", data.id := r.id, href := "#",
                    i(cls := "material-icons", "delete")
                  ),
                  deleteRecordDialog(r)
                )
              )
            })
          )
        )
      )
    )
  }

  private def editRecordDialog(record: Record): TypedTag[String] = {
    div(id:="edit-record-dialog-"+record.id, cls:="modal",
      div(cls:="modal-content",
        h4("Edit Record"),

        // in time and location
        div(cls:="row",
          div(cls:="input-field col s6",
            input(id:="in-time-input-"+record.id, tpe:="text", cls:="date-time-picker", value:=record.inTime),
            label(`for`:="in-time-input-"+record.id, cls:="active", "Current In Time: "+record.inTime)
          ),
          div(cls:="input-field col s6",
            input(id:="in-loc-input-"+record.id, tpe:="text", value:=record.inLoc),
            label(`for`:="in-loc-input-"+record.id, cls:="active", "Current In Location: "+record.inLoc)
          )
        ),

        // out time and location
        div(cls:="row",
          div(cls:="input-field col s6",
            input(id:="out-time-input-"+record.id, tpe:="text", cls:="date-time-picker", value:=record.outTime),
            label(`for`:="out-time-input-"+record.id, cls:="active", "Current Out Time: "+record.outTime)
          ),
          div(cls:="input-field col s6",
            input(id:="out-loc-input-"+record.id, tpe:="text", value:=record.outLoc),
            label(`for`:="out-loc-input-"+record.id, cls:="active", "Current Out Location: "+record.outLoc)
          )
        )
      ),
      div(cls:="modal-footer",
        a(id:="update-record-btn", data.id:=record.id, href:="#!", cls:="modal-action modal-close waves-effect btn-flat blue-text", "Update"),
        a(href:="#!", cls:="modal-action modal-close waves-effect btn-flat blue-text", "Cancel")
      )
    )
  }

  private def deleteRecordDialog(record: Record): TypedTag[String] = {
    div(id:="delete-record-dialog-"+record.id, cls:="modal",
      div(cls:="modal-content",
        h4("Delete Record"),
        p(s"In Time: ",      b(record.inTime),  br,
          s"In Location: ",  b(record.inLoc),   br,
          s"Out Time: ",     b(record.outTime), br,
          s"Out Location: ", b(record.outLoc)
        )
      ),
      div(cls:="modal-footer",
        a(id:="delete-record-btn", data.id:=record.id, href:="#!", cls:="modal-action modal-close waves-effect btn-flat blue-text", "Delete"),
        a(href:="#!", cls:="modal-action modal-close waves-effect btn-flat blue-text", "Cancel")
      )
    )
  }

  private def emailTimeSheetDialog(): TypedTag[String] = {
    val now = Dynamic.global.moment()
    val (y, m, d) = (now.year().toString.toInt, now.month().toString.toInt, now.date().toString.toInt)
    val years = y.to(y - 5, -1)
    val months = Seq("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    div(id:="email-dialog", cls:="modal modal-fixed-footer",
      div(cls:="modal-content",
        h4("E-mail Time Sheet"),
        p("test"),
        div(cls:="row",
          div(id:="year", cls:="input-field col s12 l4",
            select(name:="ts-year",
              option( value:="", disabled, "Year"),
              years.map {
                case year if year == y => option(value:=s"$year", selected, year)
                case year              => option(value:=s"$year", year)
              }
            )
          ),
          div(id:="month", cls:="input-field col s12 l4",
            select(name:="ts-month",
              option( value:="", disabled, "Month"),
              months.zipWithIndex.map {
                case (month, index) if index == m => option(value:=s"${index+1}", selected, month)
                case (month, index)               => option(value:=s"${index+1}", month)
              }
            )
          ),
          div(id:="half-month", cls:="input-field col s12 l4",
            select(name:="ts-half-month",
              option( value:="", disabled, "Half-month"),
              if (d < 16) {
                Seq(
                  option(value:="first", "First half (1 - 15)", selected),
                  option(value:="second", "Second half (16 - end of month)")
                )
              } else {
                Seq(
                  option(value:="true", "First half (1 - 15)"),
                  option(value:="false", "Second half (16 - end of month)", selected)
                )
              }
            )
          )
        )
      ),
      div(cls:="modal-footer",
        a(id:="ts-send-btn", href:="#!", cls:="modal-action modal-close waves-effect btn-flat blue-text", "Send"),
        a(href:="#!", cls:="modal-action modal-close waves-effect btn-flat blue-text", "Cancel")
      )
    )
  }
}
