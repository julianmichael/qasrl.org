package qasrl.bank

case class AnswerSource(turkerId: String, round: AnnotationRound)
object AnswerSource {
  private[this] val TurkerMatch = "turk-qasrl2.0-([0-9]+)-?(.*)".r
  import AnnotationRound._
  def fromString(s: String) = s match {
    case TurkerMatch(id, round) =>
      AnswerSource(
        id, round match {
          case "" => Original
          case "expansion" => Expansion
          case "eval" => Eval
        }
      )
  }
}
