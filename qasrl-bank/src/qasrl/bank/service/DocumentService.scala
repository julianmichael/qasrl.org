package qasrl.bank.service

import qasrl.bank._

import qasrl.data.Sentence

import cats._
import cats.implicits._
import cats.free.Free

import nlpdata.util.LowerCaseStrings._

trait DocumentService[M[_]] {
  def getDataIndex: M[DataIndex]
  def getDocument(id: DocumentId): M[Document]
  def searchDocuments(query: Set[LowerCaseString]): M[Set[DocumentId]]
}
object DocumentService {
  sealed trait RequestA[A]
  case object GetDataIndex extends RequestA[DataIndex]
  case class GetDocument(id: DocumentId) extends RequestA[Document]
  case class SearchDocuments(query: Set[LowerCaseString]) extends RequestA[Set[DocumentId]]
}

object FreeDocumentService extends DocumentService[Free[DocumentService.RequestA, ?]] {

  import DocumentService._

  type Request[A] = Free[RequestA, A]

  def getDataIndex: Request[DataIndex] =
    Free.liftF[RequestA, DataIndex](GetDataIndex)
  def getDocument(id: DocumentId): Request[Document] =
    Free.liftF[RequestA, Document](GetDocument(id))
  def searchDocuments(query: Set[LowerCaseString]): Request[Set[DocumentId]] =
    Free.liftF[RequestA, Set[DocumentId]](SearchDocuments(query))
}


class InterpretedDocumentService[M[_]](
  interpreter: DocumentService.RequestA ~> M)(
  implicit M: Monad[M]
) extends DocumentService[M] {

  override def getDataIndex: M[DataIndex] =
    FreeDocumentService.getDataIndex.foldMap(interpreter)
  override def getDocument(id: DocumentId): M[Document] =
    FreeDocumentService.getDocument(id).foldMap(interpreter)
  override def searchDocuments(query: Set[LowerCaseString]): M[Set[DocumentId]] =
    FreeDocumentService.searchDocuments(query).foldMap(interpreter)
}
