package mars

import cats.std.all._
import org.scalajs.dom._
import org.scalajs.jquery._
import upickle.default._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.scalajs.js.Dynamic
import scala.scalajs.js.annotation.JSExport
import scalatags.Text.TypedTag
import scalatags.Text.all._

@JSExport
object RecordsTableJS {

  case class Record(id: String, netId: String, inTime: String, outTime: String, inLoc: String, outLoc: String)

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
    })

    // delete icon
    $(document).on("click", "#del-record-icon", (e: Event) => {
      val id = $(e.target).closest("a").data("id")
      Dynamic.global.$("#delete-record-dialog-"+id).openModal() //show delete confirmation dialog
    })

    // delete btn in delete dialog
    $(document).on("click", "#delete-record-btn", (e: Event) => {
      val id = $(e.target).data("id").toString
      //todo: call delete on backend via REST API
//      Dynamic.global.$(e.target).closest("a").tooltip("remove")
//      $(e.target).closest("tr").empty()
    })
  }

  private def renderTable(netId: String, filterOption: String): Unit = {
    GET(s"/api/records/$netId/$filterOption").map { js =>
      val records = read[Seq[Record]](js)
      $("#record-table-container").empty().append(recordsTable(records, filterOption).render)

      // init Js libraries stuff
      Dynamic.global.$(".dropdown-button").dropdown()
      $(document).ready(Dynamic.global.$(".tooltipped").tooltip())
      Dynamic.global.$(".date-time-picker").bootstrapMaterialDatePicker(Dynamic.literal(format=timeFormat, shortTime=true))
    }
  }

  private def recordsTable(records: Seq[Record], currentFilter: String): TypedTag[String] = {
    div(`class`:="card",
      div(`class`:="card-content",
        span(`class`:="card-title",
          b("Clock In/Out Records"),
          a(`class`:="dropdown-button btn-flat right", href:="#", data("activates"):="dropdown1",
            currentFilter,
            i(`class`:="material-icons right", "filter_list")
          ),
          ul(id:="dropdown1", `class`:="dropdown-content",
            li(a( id:="filter", data.value:="pay-period", href:="#!","pay-period")),
            li(a( id:="filter", data.value:="month", href:="#!","month")),
            li(a( id:="filter", data.value:="year", href:="#!","year")),
            li(a( id:="filter", data.value:="all", href:="#!","all"))
          )
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
                td(r.inTime.toString), td(r.inLoc), td(r.outTime), td(r.outLoc),
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
}
