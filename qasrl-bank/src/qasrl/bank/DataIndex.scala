package qasrl.bank

import scala.collection.immutable.SortedSet

case class DataIndex(
  documents: Map[DatasetPartition, SortedSet[DocumentMetadata]],
  denseIds: Set[SentenceId]
) {
  lazy val allDocuments = documents.values.reduce(_ union _)
  def numDocuments = allDocuments.size
  def getPart(id: DocumentId): DatasetPartition = documents.find(_._2.exists(_.id == id)).get._1
}
