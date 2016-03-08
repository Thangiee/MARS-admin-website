package models

import java.net.HttpCookie

import cats.data.{Xor, XorT}
import cats.std.all._
import play.api.libs.json._

import scala.concurrent.Future

case class Account(netId: String, role: String, username: String, createTime: Long)

object Account {

  implicit val accFmt = Json.format[Account]

  def authenticate(user: String, passwd: String)(implicit ec: ExeCtx): Response[(IndexedSeq[HttpCookie], Account)] = {
    XorT(Future {
      val response = POST("/session/login").auth(user, passwd).asString
      response.code match {
        case 200  => Xor.Right((response.cookies, Json.parse(response.body).as[Account]))
        case code => Xor.Left(Error(code, response.body))
      }
    })
  }

  def current()(implicit req: Request, ex: ExeCtx): Response[Account] =
    call(GET("/account")).map(_.as[Account])
}