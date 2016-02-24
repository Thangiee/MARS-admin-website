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

  @JSExport
  def init(netId: String): Unit = {
    // initial render of the table when page loads
    renderTable(netId, "pay-period")

    // filter btn
    $(document).on("click", "#filter", (e: Event) => {
      val filter = $(e.target).data("value").toString
      renderTable(netId, filter)
    })

    // edit btn
    $(document).on("click", "#edit-record", (e: Event) => {
      alert("todo")
    })

    // delete btn
    $(document).on("click", "#del-record", (e: Event) => {
      //todo: show delete confirmation dialog
      //todo: call delete on backend via REST API
      Dynamic.global.$(e.target).closest("a").tooltip("remove")
      $(e.target).closest("tr").empty()
    })
  }

  private def renderTable(netId: String, filterOption: String): Unit = {
    GET(s"/api/records/$netId/$filterOption").map { js =>
      val records = read[Seq[Record]](js)
      $("#record-table-container").empty().append(recordsTable(records, filterOption).render)
      // init Js libraries stuff
      Dynamic.global.$(".dropdown-button").dropdown()
      $(document).ready(Dynamic.global.$(".tooltipped").tooltip())
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
                  a(id := "edit-record", cls := "tooltipped", data.position := "left", data.delay := "50", data.tooltip := "edit", data.id := "@r.id", href := "#",
                    i(cls := "material-icons", "edit")
                  ),
                  a(id := "del-record", cls := "tooltipped", data.position := "right", data.delay := "50", data.tooltip := "delete", data.id := "@r.id", href := "#",
                    i(cls := "material-icons", "delete")
                  )
                )
              )
            })
          )
        )
      )
    )
  }
}
