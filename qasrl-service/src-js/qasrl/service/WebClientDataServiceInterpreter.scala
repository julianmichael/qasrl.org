// package qasrl.service

// import cats.~>

// import org.scalajs.dom

// import qasrl.data.Sentence

// import scala.collection.mutable

// import io.circe.{jawn => parser}

// class WebClientDataServiceInterpreter(
//   hostAjaxUri: String
// ) extends (DataService.RequestA ~> Future) {
//   import DataService._
//   def apply[A](req: RequestA[A]): Future[A] = req match {
//     // case DataService.GetAllSentenceIds => indexCache match {
//     //   case Some(index) => index
//     //   case None => sendRequest(req).map { response =>
//     //     val res = parser.parse(responses).array.get
//     //     indexCache = Some(res)
//     //     res
//     //   }
//     // }
//     case GetSentence(id) => sentenceCache.get(id) match {
//       case Some(sentence) => Future.Success(Some(Sentence))
//       case None => sendRequest(GetSentence(id)).flatMap { sentenceJsonStr =>
//         if(sentenceJsonStr.isEmpty) Future.Success(None)
//         else parser.decode[Sentence](sentenceJsonStr) match {
//           case Left(err) => Future.Failure(err)
//           case Right(sentence) => Future.Success(Some(sentence))
//         }
//       }
//     }
//   }

//   // private[this] val indexCache: Option[SentenceMetadata] = None
//   private[this] val sentenceCache = mutable.Map[String, Sentence]

//   private[this] def sendRequest[A](req: RequestA[A]) = {
//     import scala.concurrent.ExecutionContext.Implicits.global
//     import io.circe.auto._
//     val printer = io.circe.Printer.noSpaces
//     dom.ext.Ajax.post(url = hostAjaxUri, data = printer.pretty(request.asJson))
//   }
// }

// class WebClientDataService(
//   hostAjaxUri: String
// ) extends InterpretedDataService(DataServiceInterpreter(hostAjaxUri))
