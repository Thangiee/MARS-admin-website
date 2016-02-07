package mars

import scala.scalajs.js
import org.scalajs.jquery.jQuery
import org.scalajs.dom
import shared.SharedMessages

import scala.scalajs.js.annotation.JSExport

object ScalaJSExample extends js.JSApp {
  def main(): Unit = {
    jQuery("#sup").click(() => dom.alert("OK"))
  }

}
