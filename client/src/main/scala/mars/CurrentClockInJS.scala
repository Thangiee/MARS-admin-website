package mars

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all.{icon => _, _}
import org.scalajs.dom._
import upickle.default._
import mars.components.Materialize._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

@JSExport
object CurrentClockInJS {

  case class ClockInAsst(netId: String, imgId: String, fName: String, lName: String, duration: Int, inLoc: String)

  @JSExport
  def init(wsToken: String): Unit = {
    val url = s"ws://52.33.35.165:8080/ws/current-clock-in-assts-tracker/$wsToken"

    case class State(ws: Option[WebSocket], assts: Vector[ClockInAsst], accumulator: Int)

    class Backend($: BackendScope[Unit, State]) {
      def render(state: State) = {
        card(
          cardContent(
            cardTitle(b("Currently Clock In Assistants")),

            table(`class`:="highlight",
              thead(
                tr(
                  th("Assistant"),
                  th("Duration"),
                  th("Location"),
                  th()
                )
              ),
              tbody(
                state.assts.map { asst =>
                  val duration = asst.duration + state.accumulator
                  val h = duration / 3600
                  val m = (duration / 60) % 60
                  val s = duration % 60
                  tr(id:="clock-in-asst-row",
                    td(vAlignWrapper,
                      asst.imgId match {
                        case ""     => icon(cls:="profile", "account_circle")
                        case imgUrl => img(src := imgUrl + "?size=48", cls := "circle")
                      },
                      p(s"${asst.fName.capitalize} ${asst.lName.capitalize}", vAlign, flowText)
                    ),
                    td(p(flowText, f"${h}h $m%02dm $s%02ds")),
                    td(p(flowText, asst.inLoc)),
                    td(
                      a(id := "edit-record-icon", cls := "tooltipped", href := s"/assistants/detail/${asst.netId}",
                        icon(cls:="small", "search")
                      )
                    )
                  )
                }
              )
            )
          )
        )
      }

      def tick = $.modState(s => s.copy(accumulator = s.accumulator + 1))

      def start: Callback = {

        // This will establish the connection and return the WebSocket
        def connect = CallbackTo[WebSocket] {

          // Get direct access so WebSockets API can modify state directly
          // (for access outside of a normal DOM/React callback).
          val direct = $.accessDirect

          // These are message-receiving events from the WebSocket "thread".

          def onOpen(e: Event): Unit = {
            // Indicate the connection is open
            println("Connected.")
          }

          def onMessage(e: MessageEvent): Unit = {
            val msg = e.data.toString
            if (msg.nonEmpty) direct.modState(_.copy(assts = read[Vector[ClockInAsst]](msg), accumulator = 0))
          }

          def onerror(e: ErrorEvent): Unit = {
            // Display error message
            println(s"Error: ${e.message}")
          }

          def onClose(e: CloseEvent): Unit = {
            // Close the connection
            direct.modState(_.copy(ws = None))
          }

          // Create WebSocket and setup listeners
          val ws = new WebSocket(url)
          ws.onopen = onOpen _
          ws.onclose = onClose _
          ws.onmessage = onMessage _
          ws.onerror = onerror _
          ws
        }

        // Here use attemptTry to catch any exceptions in connect.
        connect.attempt.map {
          case Right(ws)    => $.modState(_.copy(ws = Some(ws))); js.timers.setInterval(1000)(tick.runNow())
          case Left(error)  => println(error.toString)
        }
      }

      def end: Callback = {
        def closeWebSocket = $.state.map(_.ws.foreach(_.close()))
        def clearWebSocket = $.modState(_.copy(ws = None))
        closeWebSocket >> clearWebSocket
      }
    }

    val WebSocketsApp = ReactComponentB[Unit]("WebSocketsApp")
      .initialState(State(None, Vector.empty, 0))
      .renderBackend[Backend]
      .componentDidMount(_.backend.start)
      .componentWillUnmount(_.backend.end)
      .buildU

    ReactDOM.render(WebSocketsApp(), document.getElementById("react-current-clock-in"))
  }

}
