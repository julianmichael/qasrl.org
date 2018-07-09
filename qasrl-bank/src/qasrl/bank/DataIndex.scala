package qasrl.bank

import scala.collection.SortedSet

case class DataIndex(
  documents: Map[DatasetPartition, SortedSet[DocumentMetadata]],
  denseIds: Set[SentenceId]
) {
  // lazy val allIds = sentenceIdToPart.keySet
  // lazy val allSorted = allIds.toList.sorted
  // lazy val sentenceIdsByDocument = sentenceIdToPart.keySet
  //   .groupBy(_.documentId)
  //   .map { case (docId, sentenceIdSet) => docId -> sentenceIdSet.sorted }
  // lazy val allDocumentIdsSorted = sentenceIdsByDocument.sorted
}
