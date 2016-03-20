package mars

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all._
import org.scalajs.dom._
import upickle.default._

import scala.scalajs.js
import scala.scalajs.js.Dynamic
import scala.scalajs.js.annotation.JSExport

@JSExport
object CurrentClockInJS {

  case class ClockInAsst(netId: String, imgId: String, fName: String, lName: String, inTime: Double, inLoc: String)

  @JSExport
  def init(wsToken: String): Unit = {
    val url = s"ws://52.33.35.165:8080/ws/current-clock-in-assts-tracker/$wsToken"

    case class State(ws: Option[WebSocket], assts: Vector[ClockInAsst], now: Double)

    class Backend($: BackendScope[Unit, State]) {
      def render(state: State) = {
        div(`class`:="card",
          div(`class`:="card-content",
            span(`class`:="card-title",
              b("Currently Clock In Assistants")
            ),

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
                  val duration = Dynamic.global.moment.duration(state.now - asst.inTime)
                  val h = duration.asHours().toString.takeWhile(_ != '.')
                  val m = duration.minutes().toString.toInt
                  val s = duration.seconds().toString.toInt
                  tr(id:="clock-in-asst-row",
                    td(cls:="valign-wrapper",
                      img(src:=asst.imgId+"?size=48", cls:="circle"),
                      p(s"${asst.fName.capitalize} ${asst.lName.capitalize}", cls:="valign flow-text")
                    ),
                    td(p(cls:="flow-text", f"${h}h $m%02dm $s%02ds")),
                    td(p(cls:="flow-text", asst.inLoc)),
                    td(
                      a(id := "edit-record-icon", cls := "tooltipped", href := s"/assistants/detail/${asst.netId}",
                        i(cls := "small material-icons", "search")
                      )
                    )
                  )
                }
              )
            )
          )
        )
      }

      def tick = $.modState(s => s.copy(now = System.currentTimeMillis))

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
            if (msg.nonEmpty) direct.modState(_.copy(assts = read[Vector[ClockInAsst]](msg)))
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
      .initialState(State(None, Vector.empty, System.currentTimeMillis))
      .renderBackend[Backend]
      .componentDidMount(_.backend.start)
      .componentWillUnmount(_.backend.end)
      .buildU

    ReactDOM.render(WebSocketsApp(), document.getElementById("react-current-clock-in"))
  }

}
