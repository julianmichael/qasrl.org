package qasrl.bank

import scala.collection.immutable.TreeSet

import java.nio.file.Path
import java.nio.file.Files

import cats.Id

import qasrl.data.Dataset
import qasrl.data.Sentence

class Data(qasrlBankPath: Path) {
  import Data._

  lazy val trainExpanded = readDataset(qasrlBankPath.resolve("expanded").resolve("train.jsonl.gz"))
  lazy val devExpanded   = readDataset(qasrlBankPath.resolve("expanded").resolve("dev.jsonl.gz"))
  lazy val trainOrig     = filterExpandedToOrig(trainExpanded)
  lazy val devOrig       = filterExpandedToOrig(devExpanded)
  lazy val testOrig      = readDataset(qasrlBankPath.resolve("orig"    ).resolve("test.jsonl.gz"))
  lazy val devDense      = readDataset(qasrlBankPath.resolve("dense"   ).resolve("dev.jsonl.gz"))
  lazy val testDense     = readDataset(qasrlBankPath.resolve("dense"   ).resolve("test.jsonl.gz"))

  lazy val trainOrig2 = readDataset(qasrlBankPath.resolve("orig").resolve("train.jsonl.gz"))
  lazy val devOrig2 =   readDataset(qasrlBankPath.resolve("orig").resolve("dev.jsonl.gz"))

  import cats.implicits._
  implicit val datasetMonoid = Dataset.datasetMonoid(Dataset.printMergeErrors)
  lazy val all = trainExpanded |+| devExpanded |+| testOrig |+| devDense |+| testDense

  lazy val sentenceIdToPart = (
    trainExpanded.sentences.keySet.map(s => SentenceId.fromString(s) -> DatasetPartition.Train) ++
      devExpanded.sentences.keySet.map(s => SentenceId.fromString(s) -> DatasetPartition.Dev) ++
      testOrig.sentences.keySet.map(s => SentenceId.fromString(s) -> DatasetPartition.Test)
  ).toMap

  lazy val denseIds = (devDense.sentences.keySet ++ testDense.sentences.keySet)
    .map(SentenceId.fromString)

  import nlpdata.datasets.wiki1k._
  import nlpdata.datasets.tqa._
  import cats.Order.catsKernelOrderingForOrder
  import scala.util.Try

  def indexDocuments(
    Wiki1k: Wiki1kService[Try],
    tqaTopicIdToName: Map[String, String]
  ): (DataIndex, Map[DocumentId, Document]) = {
    val sentencesByDocId = all.sentences.values.toSet.groupBy((s: Sentence) => SentenceId.fromString(s.sentenceId).documentId)
    val documents = sentencesByDocId.iterator.map {
      case (docId @ DocumentId(domain, idString), sentences) =>
        def makeDocumentMetadata(title: String) = DocumentMetadata(
          docId, sentenceIdToPart(SentenceId.fromString(sentences.head.sentenceId)), title
        )
        val metadata = domain match {
          case Domain.Wikipedia =>
            makeDocumentMetadata(Wiki1k.getFile(Wiki1kPath("wikipedia", idString)).get.title)
          case Domain.Wikinews  =>
            makeDocumentMetadata(Wiki1k.getFile(Wiki1kPath("wikinews",  idString)).get.title)
          case Domain.TQA       =>
            makeDocumentMetadata(tqaTopicIdToName(idString))
        }
        Document(metadata, TreeSet(sentences.toSeq: _*))
    }.toSeq
    val documentMetasByPart = documents.groupBy(_.metadata.part).map {
      case (part, docs) => part -> TreeSet(docs.map(_.metadata): _*)
    }
    val index = DataIndex(
      documentMetasByPart,
      denseIds
    )
    val documentsById = documents.map(doc => doc.metadata.id -> doc).toMap
    (index, documentsById)
  }
}
object Data {
  def filterExpandedToOrig(dataset: Dataset) = {
    val withoutQuestions = dataset.filterQuestionSources(qs => QuestionSource.fromString(qs).isTurker)
    val withoutAnswers   = Dataset.questionLabels.modify(ql =>
      ql.copy(
        answerJudgments = ql.answerJudgments.filter(
          al => AnswerSource.fromString(al.sourceId).round.isOriginal
        )
      )
    )(withoutQuestions)
    withoutAnswers
  }

  def constructTQATopicIdToNameMapping(path: Path): Map[String, String] = {
    val json = io.circe.jawn.parseFile(new java.io.File(path.toString)).right.get
    json.asArray.get.map { e =>
      val fields = e.asObject.get.toMap
      fields("globalID").asString.get -> fields("topicName").asString.get
    }.toSet.toMap
  }

  def readDataset(path: Path): Dataset = {
    import java.io.FileInputStream
    import java.util.zip.GZIPInputStream
    val source = scala.io.Source.fromInputStream(
      new GZIPInputStream(new FileInputStream(path.toString)) // TODO proper nio way of doing this
    )
    import qasrl.data.JsonCodecs._
    Dataset(
      source.getLines.map { line =>
        val sentence = io.circe.jawn.decode[Sentence](line).right.get
        sentence.sentenceId -> sentence
      }.toMap
    )
  }

  def writeDatasetUnzipped(path: Path, dataset: Dataset) = {
    import qasrl.data.JsonCodecs._
    import io.circe.generic.auto._
    import io.circe.syntax._
    val printer = io.circe.Printer.noSpaces
    Files.write(path, printer.pretty(dataset.asJson).getBytes("UTF-8"))
  }

  def writeDatasetJS(path: Path, dataset: Dataset) = {
    import qasrl.data.JsonCodecs._
    import io.circe.generic.auto._
    import io.circe.syntax._
    val printer = io.circe.Printer.noSpaces
    Files.write(path, ("var dataset = " + printer.pretty(dataset.asJson) + ";").getBytes("UTF-8"))
  }

  import io.circe.generic.auto._
  import io.circe.syntax._
  import cats.Order.catsKernelOrderingForOrder

  def writeIndex(path: Path, index: DataIndex) = {
    val printer = io.circe.Printer.noSpaces
    Files.write(path, printer.pretty(index.asJson).getBytes("UTF-8"))
  }

  def writeIndexJS(path: Path, index: DataIndex) = {
    val printer = io.circe.Printer.noSpaces
    val res = "var dataMetaIndex = " + printer.pretty(index.asJson) + ";"
    Files.write(path, res.getBytes("UTF-8"))
  }

  def readIndex(path: Path): DataIndex = {
    io.circe.jawn.decodeFile[DataIndex](new java.io.File(path.toString)).right.get
  }
}
