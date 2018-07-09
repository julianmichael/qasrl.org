package qasrl.bank.service

import qasrl.bank.DataIndex
import qasrl.bank.Document
import qasrl.bank.DocumentId

import cats.~>
import cats.Id

import io.circe.parser.decode

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

// TODO add *request* caching to prevent redundant api calls
case class WebClientDocumentServiceInterpreter(
  apiUrl: String
) extends (DocumentService.RequestA ~> CacheCall) {
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

  def apply[A](req: RequestA[A]): CacheCall[A] = {
    implicit val did = dataIndexDecoder
    implicit val dd = documentDecoder

    req match {
      case GetDataIndex => indexCache match {
        case Some(index) => Cached(index)
        case None => Remote(
          sendRequest(req).map(_.responseText).flatMap { dataIndexJsonStr =>
            decode[DataIndex](dataIndexJsonStr) match {
              case Left(err) =>
                Future.failed[DataIndex](new RuntimeException(err))
              case Right(index) =>
                indexCache = Some(index)
                Future.successful(index)
            }
          }
        )
      }
      case GetDocument(id) => documentCache.get(id) match {
        case Some(document) => Cached(document)
        case None => Remote(
          sendRequest(GetDocument(id)).map(_.responseText).flatMap { documentJsonStr =>
            decode[Document](documentJsonStr) match {
              case Left(err) => Future.failed[Document](new RuntimeException(err))
              case Right(document) =>
                documentCache.put(id, document)
                Future.successful(document)
            }
          }
        )
      }
    }
  }

  private[this] var indexCache: Option[DataIndex] = None
  private[this] val documentCache = mutable.Map.empty[DocumentId, Document]

  private[this] def sendRequest[A](req: RequestA[A]) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    org.scalajs.dom.ext.Ajax.get(url = apiUrl + "/" + getRoute(req))
  }

  private[this] def getRoute[A](req: RequestA[A]): String = req match {
    case GetDataIndex => s"index"
    case GetDocument(id) => s"doc/${id.domain}/${id.id}"
  }
}

class WebClientDocumentService(
  apiUrl: String,
) extends InterpretedDocumentService[CacheCall](
  WebClientDocumentServiceInterpreter(apiUrl)
)(CacheCall.cacheCallInstance)
