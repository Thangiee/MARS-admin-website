package models

import java.net.HttpCookie

import cats.data.{Xor, XorT}
import cats.std.all._
import controllers.Forms.CreateAcc
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

  def logout()(implicit req: Request, exeCtx: ExeCtx): Response[Unit] = call(POST("/session/logout")).map(_ => ())

  def current()(implicit req: Request, ec: ExeCtx): Response[Account] = call(GET("/account")).map(_.as[Account])

  def delete(netId: String)(implicit req: Request, ec: ExeCtx): Response[Unit] = call(DELETE(s"/account/$netId")).map(_ => ())

  def approve(netId: String)(implicit req: Request, ec: ExeCtx): Response[Unit] =
    call(POST(s"/account/change-approve/$netId").postForm.param("approve", "true")).map(_ => ())

  def changePasswd(netId: String, newPass: String)(implicit req: Request, ec: ExeCtx): Response[Unit] =
    call(POST(s"/account/change-password/$netId").postForm.param("new_password", newPass)).map(_ => ())

  def resetPasswd(token: String, newPass: String)(implicit req: Request, ec: ExeCtx): Response[Unit] =
    call(POST(s"/token/change-password").postForm.params("new_pass" -> newPass, "token" -> token)).map(_ => ())

  def emailPasswdReset(netId: String)(implicit req: Request, ec: ExeCtx): Response[Unit] = {
    val email =
      s"""
         |We received a request to reset the password associated with this e-mail address. If you made this request, please follow the instructions below.
         |Click the link below to reset your password:
         |
         |http://localhost:9000/password-reset?token=<token>
         |
         |If you did not request to have your password reset you can safely ignore this email.
        """.stripMargin
    println(netId, email)
    call(POST(s"/email/password-reset").postForm.params("net_id" -> netId, "email_body" -> email)).map(_ => ())
  }

  def create(form: CreateAcc)(implicit req: Request, ec: ExeCtx): Response[Unit] =
    call(POST("/account/instructor").postForm.params(
      "net_id" -> form.netId, "user" -> form.username, "pass" -> form.passwd,
      "email" -> form.email, "first" -> form.firstName, "last" -> form.lastName)
    ).map(_ => ())
}