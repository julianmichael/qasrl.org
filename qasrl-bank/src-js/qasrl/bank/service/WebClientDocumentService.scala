package qasrl.bank.service

import qasrl.bank.DataIndex
import qasrl.bank.Document
import qasrl.bank.DocumentId

import cats.Monad
import cats.~>
import cats.Id
import cats.implicits._

import io.circe.parser.decode

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

// TODO add *request* caching to prevent redundant api calls
// TODO make general http GET client from service:
// * api URL
// * function from request to route
// * function from request ~> Decoder
case class WebClientDocumentServiceInterpreter(
  apiUrl: String
) extends (DocumentService.Request ~> Future) {
  import DocumentService._

  import cats.Order.catsKernelOrderingForOrder
  import io.circe.Decoder

  val dataIndexDecoder: Decoder[DataIndex] = {
    import io.circe.generic.auto._
    implicitly[Decoder[DataIndex]]
  }
  val documentDecoder: Decoder[Document] = {
    import qasrl.data.JsonCodecs._
    import io.circe.generic.auto._
    import qasrl.bank.qasrlDataSentenceOrder
    implicitly[Decoder[Document]]
  }
  val documentIdSetDecoder: Decoder[Set[DocumentId]] = {
    import qasrl.data.JsonCodecs._
    import io.circe.generic.auto._
    import qasrl.bank.qasrlDataSentenceOrder
    implicitly[Decoder[Set[DocumentId]]]
  }

  def apply[A](req: Request[A]): Future[A] = {
    implicit val did = dataIndexDecoder
    implicit val dd = documentDecoder
    implicit val disd = documentIdSetDecoder

    req match {
      case GetDataIndex =>
        sendRequest(req).map(_.responseText).flatMap { dataIndexJsonStr =>
          decode[DataIndex](dataIndexJsonStr) match {
            case Left(err)    => Future.failed[DataIndex](new RuntimeException(err))
            case Right(index) => Future.successful(index)
          }
        }
      case GetDocument(id) => {
        sendRequest(GetDocument(id)).map(_.responseText).flatMap { documentJsonStr =>
          decode[Document](documentJsonStr) match {
            case Left(err)       => Future.failed[Document](new RuntimeException(err))
            case Right(document) => Future.successful(document)
          }
        }
      }
      case SearchDocuments(query) => {
        sendRequest(SearchDocuments(query)).map(_.responseText).flatMap { documentIdSetJsonStr =>
          decode[Set[DocumentId]](documentIdSetJsonStr) match {
            case Left(err)          => Future.failed[Set[DocumentId]](new RuntimeException(err))
            case Right(documentIds) => Future.successful(documentIds)
          }
        }
      }
    }
  }

  private[this] def sendRequest[A](req: Request[A]) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    org.scalajs.dom.ext.Ajax.get(url = apiUrl + "/" + getRoute(req))
  }

  private[this] def getRoute[A](req: Request[A]): String = req match {
    case GetDataIndex => s"index"
    case GetDocument(id) => s"doc/${id.domain}/${id.id}"
    case SearchDocuments(query) => s"search/${query.mkString(" ")}"
  }
}

class WebClientDocumentService(
  apiUrl: String
) extends InterpretedDocumentService[Future](
  WebClientDocumentServiceInterpreter(apiUrl)
)
