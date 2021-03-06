package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._

object Forms {

  case class CreateAcc(netId: String, username: String, passwd: String, passwd2: String, email: String, lastName: String, firstName: String)
  val createAcc = Form(
    mapping(
      "netid"      -> nonEmptyText,
      "username"   -> nonEmptyText,
      "passwd"     -> nonEmptyText(minLength = 6),
      "passwd2"    -> nonEmptyText(minLength = 6),
      "email"      -> email,
      "last-name"  -> nonEmptyText,
      "first-name" -> nonEmptyText
    )(CreateAcc.apply)(CreateAcc.unapply)
  )

  case class UpdateAsst(empId: String, payRate: Double, job: String, dept: String, title: String, code: String, thres: Double)
  val updateAsst = Form(
    mapping(
      "emp-id"   -> nonEmptyText,
      "pay-rate" -> of[Double],
      "job"      -> nonEmptyText,
      "dept"     -> nonEmptyText,
      "title"    -> nonEmptyText,
      "code"     -> nonEmptyText,
      "thres"    -> of[Double].verifying(min(0.0), max(1.0))
    )(UpdateAsst.apply)(UpdateAsst.unapply)
  )

  case class UpdateRecord(inTime: Long, inLoc: String, outTime: Long, outLoc: String)
  val updateRecord = Form(
    mapping(
      "in-time" -> longNumber,
      "in-loc"   -> text,
      "out-time" -> longNumber,
      "out-loc"  -> text
    )(UpdateRecord.apply)(UpdateRecord.unapply).verifying("In time must be before out time.", r => r.inTime <= r.outTime)
  )

  val deleteFace = Form("face-id" -> nonEmptyText)

  val login = Form(
    tuple(
      "login-user" -> nonEmptyText.verifying("Can't have space", !_.contains(" ")),
      "login-passwd" -> nonEmptyText
    )
  )

  val emailPasswdReset = Form("net-id" -> nonEmptyText)

  val changePasswd = Form("new-passwd" -> nonEmptyText(minLength = 6).verifying("Can't have space", !_.contains(" ")))

  val changeInstructorRole = Form("is-admin" -> boolean)
}
