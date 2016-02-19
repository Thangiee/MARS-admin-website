package mars

import org.scalajs.jquery.jQuery

import scala.scalajs.js

object ScalaJSMain extends js.JSApp {
  def main(): Unit = {
    jQuery(AssistantSearchJS.init _)
  }
}
