package qasrl.service

import qasrl.data.Sentence

import cats._
import cats.implicits._
import cats.free.Free

trait DataService[M[_]] {

  implicit protected val monad: Monad[M]

  // def getAllSentenceIds: M[List[SentenceMetadata]]

  def getSentence(id: String): M[Option[Sentence]]
}

object DataService {
  sealed trait RequestA[A]
  // case object GetAllSentenceIds extends RequestA[List[SentenceMetadata]]
  case class GetSentence(id: String) extends RequestA[Option[Sentence]]
}

object FreeDataService extends DataService[Free[DataService.RequestA, ?]] {

  import DataService._

  type Request[A] = Free[RequestA, A]

  protected override val monad: Monad[Request] = implicitly[Monad[Request]]

  // def getAllSentenceIds: DataServiceRequest[List[SentenceMetadata]] =
  //   Free.liftF[DataService.RequestA, List[SentenceMetadata]](GetAllSentenceIds)

  def getSentence(id: String): Request[Option[Sentence]] =
    Free.liftF[RequestA, Option[Sentence]](GetSentence(id))
}


class InterpretedDataService[M[_]](
  interpreter: DataService.RequestA ~> M)(
  protected override implicit val monad: Monad[M]
) extends DataService[M] {

  // override def getAllPaths: M[List[SentenceMetadata]] =
  //   FreeDataService.getAllPaths.foldMap(interpreter)

  override def getSentence(id: String): M[Option[Sentence]] =
    FreeDataService.getSentence(id).foldMap(interpreter)
}
