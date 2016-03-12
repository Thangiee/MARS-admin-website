import cats.data.XorT

import scala.concurrent.Future

package object mars {
  type Response[R] = XorF[Error, R]
  type XorF[L, R] = XorT[Future, L, R]
  case class Error(code: Int, msg: String)

  case class Record(id: String, netId: String, inTime: String, outTime: String, inLoc: String, outLoc: String)
}
