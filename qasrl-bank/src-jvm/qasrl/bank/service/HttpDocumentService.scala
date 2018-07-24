package qasrl.bank

import nlpdata.util.LowerCaseStrings._

import cats.implicits._
import cats.effect._

import org.http4s._
import org.http4s.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

object HttpDocumentService {

  import cats.Order.catsKernelOrderingForOrder
  import io.circe.Encoder

  val dataIndexEncoder: Encoder[DataIndex] = {
    import io.circe.generic.auto._
    implicitly[Encoder[DataIndex]]
  }
  val documentEncoder: Encoder[Document] = {
    import qasrl.data.JsonCodecs._
    import io.circe.generic.auto._
    implicitly[Encoder[Document]]
  }
  val documentIdSetEncoder: Encoder[collection.immutable.Set[DocumentId]] = {
    import qasrl.data.JsonCodecs._
    import io.circe.generic.auto._
    implicitly[Encoder[collection.immutable.Set[DocumentId]]]
  }

  def makeService(
    index: DataIndex,
    documents: Map[DocumentId, Document],
    searchIndex: Map[LowerCaseString, Set[DocumentId]]
  ) = {
    implicit val die = dataIndexEncoder
    implicit val de = documentEncoder
    implicit val dise = documentIdSetEncoder
    import io.circe.syntax._
    import org.http4s.dsl.io._
    import org.http4s.circe._
    HttpService[IO] {
      case GET -> Root / "index" =>
        Ok(index.asJson)
      case GET -> Root / "doc" / domain / id =>
        Ok(documents(DocumentId(Domain.fromString(domain.lowerCase).get, id)).asJson)
      case GET -> Root / "search" / query =>
        val keywords = query.split(" ").map(_.lowerCase).toSet
        if(keywords.isEmpty) {
          Ok(documents.keySet.asJson)
        } else {
          val results: Set[DocumentId] = keywords
            .map(w => searchIndex.get(w).getOrElse(Set.empty[DocumentId]))
            .reduce(_ intersect _)
          Ok(results.asJson)
        }
    }
  }
}
