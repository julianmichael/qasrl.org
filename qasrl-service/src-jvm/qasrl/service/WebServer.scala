package qasrl.service

import qasrl.data.Dataset
import qasrl.data.JsonCodecs._

import cats.data.Ior

// import io.circe._
import io.circe.syntax._

// import org.http4s._

// class WebServer(
//   dataset: Dataset,
//   config: Ior[WebServer.HttpConfig, WebServer.HttpsConfig]
// ) {
//   val http = HttpRoutes.of {
//     case POST -> Root / sentenceId =>
//       val jsonPrinter = io.circe.Printer.noSpaces
//       val res = dataset.sentences.get(sentenceId)
//         .map(_.asJson)
//         .map(jsonPrinter.pretty)
//         .getOrElse("")
//       Ok(res)
//   }
// }
// object WebServer {
//   case class HttpConfig(port: Int)
//   case class HttpsConfig(port: Int, certFilePath: String) // TODO actual thing
// }
