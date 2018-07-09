package qasrl.bank

import io.circe.{KeyEncoder, KeyDecoder}

sealed trait DatasetPartition
object DatasetPartition {
  case object Train extends DatasetPartition
  case object Dev   extends DatasetPartition
  case object Test  extends DatasetPartition

  implicit val datasetPartitionKeyEncoder = new KeyEncoder[DatasetPartition] {
    override def apply(part: DatasetPartition): String = part match {
      case Train => "train"
      case Dev   => "dev"
      case Test  => "test"
    }
  }

  implicit val datasetPartitionKeyDecoder = new KeyDecoder[DatasetPartition] {
    override def apply(key: String): Option[DatasetPartition] = key match {
      case "train" => Some(Train)
      case "dev"   => Some(Dev)
      case "test"  => Some(Test)
      case _ => None
    }
  }
}
