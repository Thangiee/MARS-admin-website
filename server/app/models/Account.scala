package models

import java.net.HttpCookie

import cats.data.{XorT, Xor}
import cats.std.all._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

case class Account(netId: String, role: String, username: String, createTime: Long, passwd: String)

object Account {

  implicit val accFmt = Json.format[Account]

  def authenticate(user: String, passwd: String)(implicit ec: ExecutionContext): XorF[Error, (IndexedSeq[HttpCookie], Account)] = {
    XorT(Future {
      val response = POST("/session/login").auth(user, passwd).asString
      response.code match {
        case 200  => Xor.Right((response.cookies, Json.parse(response.body).as[Account]))
        case code => Xor.Left(Error(code, response.body))
      }
    })
  }

  def current()(implicit req: Request, ex: ExecutionContext): XorF[Error, Account] =
    call(GET("/account")).map(_.as[Account])

}