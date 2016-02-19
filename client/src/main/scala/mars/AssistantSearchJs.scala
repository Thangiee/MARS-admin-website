package mars

import org.scalajs.dom._
import org.scalajs.jquery._
import upickle.default._

import scala.scalajs.js.{Dynamic, JSON}
import scala.util.Random
import scalatags.Text.all._

object AssistantSearchJS {

  case class Assistant(fullName: String, netId: String, email: String, job: String)

  val $ = jQuery

  def init() = {
    val seed = Random.nextInt(9999)
    val asstsContainer = $("#assts-list")
    // parse the assistants JSON data stored in assistants.scala.html
    val assts = read[Seq[Assistant]](JSON.stringify($("#data").data("assts")))

    def initials(name: String) = name.split(" ").map(_.head.toString).reduce(_ + _)
    def genGmailLikeTextAvatars() = Dynamic.global.$(".profile").initial(Dynamic.literal(seed=seed, charCount=2, fontSize=42))

    // Map[key="fullName netId", value=html], key is used later for searching.
    val asstsMap = assts.map(asst =>
      s"${asst.fullName} ${asst.netId}".toLowerCase -> {
        a(`class`:="collection-item avatar",
          img(`class`:="profile circle", data("name"):=initials(asst.fullName)),
          span(`class`:="title", s"${asst.fullName}"),
          p(asst.email, br, "Job: "+asst.job)
        )
      }
    ).toMap

    // initial render
    asstsContainer.empty().append(asstsMap.values.toSeq.render)
    genGmailLikeTextAvatars()

    $(document).on("keyup", "input[name=asst-search]", (e: Event) => {
      val searchField = $(e.target)
      val query = searchField.`val`().toString
      val htmls = asstsMap.filter(_._1 contains query).values

      // re-render matched results
      asstsContainer.empty().append(htmls.toSeq.render)
      genGmailLikeTextAvatars()
    })
  }
}
