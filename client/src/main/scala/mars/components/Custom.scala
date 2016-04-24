package mars.components

import japgolly.scalajs.react.vdom.{Attr, ReactTagOf}
import japgolly.scalajs.react.vdom.all._
import mars.Account
import mars.components.Materialize._
import org.scalajs.dom.html.Div

object Custom {

  def accHeader(acc: Account, xs: TagMod*): ReactTagOf[Div] = {
    val initials = acc.firstName.head.toString + acc.lastName.head.toString
    collapsibleHeader(
      row(
        div(
          img(cls := "profile circle col", Attr("data-name") := initials),
          h6(cls := "col", s"Net Id: ${acc.netId}", br(), s"${acc.firstName} ${acc.lastName}", br(), acc.email)
        ),
        xs
      )
    )
  }

}
