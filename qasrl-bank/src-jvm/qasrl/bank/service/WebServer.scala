package qasrl.bank

import cats.implicits._
import cats.effect._

import org.http4s._
import org.http4s.implicits._

import org.http4s.server.blaze._

import fs2.{Stream, StreamApp}
import fs2.StreamApp.ExitCode

import scala.concurrent.ExecutionContext.Implicits.global

import nlpdata.util.Text
import nlpdata.util.LowerCaseStrings._

object WebServerMain extends StreamApp[IO] {
  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] = {

    import java.nio.file.Paths
    val data = new Data(Paths.get("../qasrl-bank/data/qasrl-v2"))

    import nlpdata.datasets.wiki1k._
    import nlpdata.datasets.tqa._
    import cats.Order.catsKernelOrderingForOrder

    val Wiki1k = new Wiki1kFileSystemService(Paths.get("../mturk-semantics/resources/wiki1k"))
    val tqaMapping = Data.constructTQATopicIdToNameMapping(Paths.get("../mturk-semantics/resources/tqa/tqa_sentences_trimmed.json"))

    println("Loading data...")
    val (index, docs) = data.indexDocuments(Wiki1k, tqaMapping)
    println("Data loaded. Indexing documents...")
    val searchIndex = {
      def tokenDocPairs = for {
        doc <- docs.values.iterator
        sent <- doc.sentences.iterator
        tokIndex <- sent.sentenceTokens.indices.iterator
        tok <- (
          List(sent.sentenceTokens(tokIndex), Text.normalizeToken(sent.sentenceTokens(tokIndex))) ++
            sent.verbEntries.get(tokIndex).fold(List.empty[String])(verb =>
              verb.verbInflectedForms.allForms.map(_.toString)
            )
        ).iterator
      } yield tok.lowerCase -> doc.metadata.id

      tokenDocPairs.toList.groupBy(_._1).map { case (tok, pairs) =>
        tok -> pairs.map(_._2).toSet
      }
    }

    val bareService = HttpDocumentService.makeService(index, docs, searchIndex)

    import org.http4s.server.middleware._

    val service = CORS(bareService)

    BlazeBuilder[IO]
      .bindHttp(8080, "localhost")
      .mountService(service, "/")
      .mountService(StaticSiteService.service, "/")
      .serve
  }
}
