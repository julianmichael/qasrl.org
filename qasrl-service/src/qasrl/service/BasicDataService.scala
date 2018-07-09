package qasrl.service

import qasrl.data.Dataset

import cats.~>
import cats.Id

case class BasicDataServiceInterpreter(
  dataset: Dataset
  // getMetadata: String => Map[String, String]
) extends (DataService.RequestA ~> Id) {
  import DataService._
  def apply[A](req: RequestA[A]): A = req match {
    // case GetAllSentenceIds => dataset.sentences.keys.toList.map(sid =>
    //   SentenceMetadata(sid, getMetadata(sid))
    // )
    case GetSentence(id) => dataset.sentences.get(id)
  }
}

class BasicDataService(
  dataset: Dataset
  // getMetadata: String => Map[String, String]
) extends InterpretedDataService(
  BasicDataServiceInterpreter(dataset)
)
