package qasrl.bank

import scala.collection.immutable.SortedSet

case class DataIndex(
  documents: Map[DatasetPartition, SortedSet[DocumentMetadata]],
  denseIds: Set[SentenceId]
) {
  lazy val numDocuments = documents.values.map(_.size).sum
}
