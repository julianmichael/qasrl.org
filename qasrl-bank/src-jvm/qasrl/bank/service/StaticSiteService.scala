package qasrl.bank

import cats.implicits._
import cats.effect._

import org.http4s._
import org.http4s.implicits._
import org.http4s.dsl.io._

object StaticSiteService {
  def service = HttpService[IO] {
    case request @ GET -> "site" /: path =>
      StaticFile.fromFile(new java.io.File(s"site/$path"), Some(request))
        .getOrElseF(NotFound())
  }
}
