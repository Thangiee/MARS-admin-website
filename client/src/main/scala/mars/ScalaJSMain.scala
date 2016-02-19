package mars

import org.scalajs.jquery.jQuery
import org.scalajs.dom._

import scala.scalajs.js

object ScalaJSMain extends js.JSApp {
  def main(): Unit = {
    jQuery(document).find("title").text() match {
      case title if title contains "Assistants" =>
        jQuery(AssistantSearchJS.init _)
      case _ =>
    }
  }
}
