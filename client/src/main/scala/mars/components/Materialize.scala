package mars.components

import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.vdom.{Attr, ClassNameAttr, ReactTagOf, TagMod}
import org.scalajs.dom.html._

object Materialize {

  val vAlignWrapper: ClassNameAttr[String] = cls := "valign-wrapper"
  val vAlign       : ClassNameAttr[String] = cls := "valign"
  val leftAlign    : ClassNameAttr[String] = cls := "left-align"
  val rightAlign   : ClassNameAttr[String] = cls := "right-align"
  val centerAlign  : ClassNameAttr[String] = cls := "center-align"
  val noPadding    : ClassNameAttr[String] = cls := "no-padding"
  val fixedFooter  : ClassNameAttr[String] = cls := "modal-fixed-footer"
  val flowText     : ClassNameAttr[String] = cls := "flow-text"

  val accordion : TagMod = Attr("data-collapsible") := "accordion"
  val expandable: TagMod = Attr("data-collapsible") := "expandable"

  val tooltip: (String, String) => Seq[TagMod] =
    (p: String, s: String) => Seq(cls := "tooltipped", position(p), Attr("data-tooltip") := s)

  def position(p: String): TagMod = Attr("data-position") := p

  def colm(s: Int, m: Int, l: Int): ClassNameAttr[String] = cls := s"col s$s m$m l$l"
  def colm(s: Int): ClassNameAttr[String] = cls := s"col s$s"

  def row(xs: TagMod*): ReactTagOf[Div] = div(cls := "row", xs)

  def secondaryContent(xs: TagMod*): ReactTagOf[Div] = div(cls := "secondary-content", xs)

  def raisedBtn(xs: TagMod*): ReactTagOf[Anchor] = a(cls := "waves-effect waves-light btn", xs)
  def flatBtn(xs: TagMod*): ReactTagOf[Anchor] = a(cls := "waves-effect btn-flat", xs)
  def submitBtn(xs: TagMod*) = button(tpe := "submit", cls := "waves-effect", href := "#!", xs)

  def icon(xs: TagMod*): ReactTagOf[Element] = i(cls := "material-icons", xs)
  def iconLeft(xs: TagMod*): ReactTagOf[Element] = i(cls := "material-icons left", xs)
  def iconRight(xs: TagMod*): ReactTagOf[Element] = i(cls := "material-icons right", xs)

  def collapsible(xs: TagMod*): ReactTagOf[UList] = ul(cls := "collapsible", xs)
  def collapsibleHeader(xs: TagMod*): ReactTagOf[Div] = div(cls := "collapsible-header", xs)
  def collapsibleBody(xs: TagMod*): ReactTagOf[Div] = div(cls := "collapsible-body", xs)

  def card(xs: TagMod*): ReactTagOf[Div] = div(cls := "card", xs)
  def cardContent(xs: TagMod*): ReactTagOf[Div] = div(cls := "card-content", xs)
  def cardAction(xs: TagMod*): ReactTagOf[Div] = div(cls := "card-action", xs)
  def cardTitle(xs: TagMod*): ReactTagOf[Span] = span(cls := "card-title", xs)

  def tabs(xs: TagMod*): ReactTagOf[UList] = ul(cls := "tabs", xs)
  def tab(xs: TagMod*): ReactTagOf[LI] = li(cls := "tab", xs)

  def modal(xs: TagMod*): ReactTagOf[Div] = div(cls := "modal", xs)
  def modalContent(xs: TagMod*): ReactTagOf[Div] = div(cls := "modal-content", xs)
  def modalFooter(xs: TagMod*): ReactTagOf[Div] = div(cls := "modal-footer", xs)

  def radio(isCheck: Boolean, defVal: String, xs: TagMod*): ReactTagOf[Input] =
    input(tpe := "radio", defaultValue := defVal, Attr("defaultChecked") := isCheck, xs)
}
