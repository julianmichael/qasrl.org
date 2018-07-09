package qasrl.bank.service

import qasrl.bank._

import qasrl.data.Sentence

import cats._
import cats.implicits._
import cats.free.Free

trait DocumentService[M[_]] {
  def getDataIndex: M[DataIndex]
  def getDocument(id: DocumentId): M[Document]
}
object DocumentService {
  sealed trait RequestA[A]
  case object GetDataIndex extends RequestA[DataIndex]
  case class GetDocument(id: DocumentId) extends RequestA[Document]
}

object FreeDocumentService extends DocumentService[Free[DocumentService.RequestA, ?]] {

  import DocumentService._

  type Request[A] = Free[RequestA, A]

  def getDataIndex: Request[DataIndex] =
    Free.liftF[RequestA, DataIndex](GetDataIndex)
  def getDocument(id: DocumentId): Request[Document] =
    Free.liftF[RequestA, Document](GetDocument(id))
}


class InterpretedDocumentService[M[_]](
  interpreter: DocumentService.RequestA ~> M)(
  implicit M: Monad[M]
) extends DocumentService[M] {

  override def getDataIndex: M[DataIndex] =
    FreeDocumentService.getDataIndex.foldMap(interpreter)
  override def getDocument(id: DocumentId): M[Document] =
    FreeDocumentService.getDocument(id).foldMap(interpreter)
}
