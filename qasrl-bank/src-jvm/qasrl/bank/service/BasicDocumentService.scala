package qasrl.bank.service

import qasrl.bank.DataIndex
import qasrl.bank.Document
import qasrl.bank.DocumentId

import cats.~>
import cats.Id

case class BasicDocumentServiceInterpreter(
  index: DataIndex,
  documents: Map[DocumentId, Document]
) extends (DocumentService.RequestA ~> Id) {
  import DocumentService._
  def apply[A](req: RequestA[A]): A = req match {
    case GetDataIndex => index
    case GetDocument(id) => documents(id)
  }
}

class BasicDocumentService(
  index: DataIndex,
  documents: Map[DocumentId, Document]
) extends InterpretedDocumentService[Id](
  BasicDocumentServiceInterpreter(index, documents)
)
