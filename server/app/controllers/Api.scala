package controllers

import cats.std.all._
import com.github.nscala_time.time.Imports._
import models._
import play.api.data.Form
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

class Api() extends Controller {

  // =============
  //    Account
  // =============

  def approve(netId: String) = Action.async(implicit request => toResult(Account.approve(netId)))

  def deleteAccount(netId: String) = Action.async(implicit request => toResult(Account.delete(netId)))

  def changePasswd(netId: String) = Action.async { implicit request =>
    Forms.changePasswd.bindFromRequest().fold(
      formErr => Future.successful(BadRequest(formErrMsg(formErr))),
      newPass => toResult(Account.changePasswd(netId, newPass))
    )
  }

  // =============
  //   Assistant
  // =============

  def allAssistants = Action.async(implicit request => toJsonResult(Assistant.all()))

  def assistant(netId: String) = Action.async(implicit request => toJsonResult(Assistant.findByNetId(netId)))

  def updateAssistant(netId: String) = Action.async { implicit request =>
    Forms.updateAsst.bindFromRequest().fold(
      formErr => Future.successful(BadRequest(formErrMsg(formErr))),
      data    => toResult(Assistant.update(netId, data.payRate, data.dept, data.title, data.code, data.thres))
    )
  }

  // =============
  //   FaceImg
  // =============

  def faceImg(netId: String) = Action.async(implicit request => toJsonResult(FaceImg.findByNetId(netId)))

  def deleteFaceImg(imgId: String) = Action.async { implicit request =>
    FaceImg.delete(imgId).fold(err => err.toStatusCode, succ => Ok)
  }

  // =============
  //  Instructor
  // =============

  def allInstructor = Action.async(implicit request => toJsonResult(Instructor.all()))

  def changeInstructorRole(netId: String) = Action.async { implicit request =>
    Forms.changeInstructorRole.bindFromRequest().fold(
      formErr => Future.successful(BadRequest(formErrMsg(formErr))),
      isAdmin => toResult(Instructor.changeRole(netId, isAdmin))
    )
  }

  // =============
  //    Record
  // =============

  def records(netId: String, filter: String) = Action.async { implicit request =>
    Record.findByNetId(netId, filter).fold(
      err     => err.toStatusCode,
      records =>
        Ok(Json.toJson(records.map(r => Map(
          "id"      -> r.id.toString,
          "netId"   -> r.netId,
          "inTime"  -> formatTime(r.inTime),
          "inLoc"   -> r.inComputerId.getOrElse(""),
          "outTime" -> r.outTime.map(formatTime).getOrElse(""),
          "outLoc"  -> r.outComputerId.getOrElse("")
        ))))
    )
  }

  def updateRecord(id: String) = Action.async { implicit request =>
    Forms.updateRecord.bindFromRequest().fold(
      formErr => Future.successful(BadRequest(formErrMsg(formErr))),
      record  => toResult(Record.update(id, record.inTime, record.outTime, record.inLoc, record.outLoc))
    )
  }

  def deleteRecord(id: String) = Action.async(implicit request => toResult(Record.delete(id)))

  // ----------------

  private def toResult[A](response: Response[A]): Future[Result] = response.fold(err => err.toStatusCode, succ => Ok)

  private def toJsonResult[W: Writes](response: Response[W]) = response.fold(err => err.toStatusCode, data => Ok(Json.toJson(data)))

  private def formatTime(ms: Long): String = DateTimeFormat.forPattern("MM/dd/YY h:mm a").print(ms)

  private def formErrMsg[A](form: Form[A]): String = form.globalErrors.toList match {
    case Nil    => form.errors.map(f => s"${f.key} ${f.message}").mkString(", ")
    case errors => errors.mkString(", ")
  }
}