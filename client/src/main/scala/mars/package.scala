import cats.data.XorT

import scala.concurrent.Future

package object mars {
  type Response[R] = XorF[Error, R]
  type XorF[L, R] = XorT[Future, L, R]
  case class Error(code: Int, msg: String)

  case class Record(id: String, netId: String, inTime: String, outTime: String, inLoc: String, outLoc: String)

  case class Assistant(
    netId: String,
    username: String,
    role: String,
    createTime: Double,
    approve: Boolean,
    rate: Double,
    email: String,
    job: String,
    department: String,
    lastName: String,
    firstName: String,
    employeeId: String,
    title: String,
    titleCode: String,
    threshold: Double
  )

  case class Instructor(
    netId: String,
    username: String,
    role: String,
    createTime: Double,
    approve: Boolean,
    email: String,
    lastName: String,
    firstName: String
  )
}
