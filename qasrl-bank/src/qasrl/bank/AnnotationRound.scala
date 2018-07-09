package qasrl.bank

sealed trait AnnotationRound {
  import AnnotationRound._
  def isOriginal  = this == Original
  def isExpansion = this == Expansion
  def isEval      = this == Eval
}
object AnnotationRound {
  case object Original extends AnnotationRound
  case object Expansion extends AnnotationRound
  case object Eval extends AnnotationRound
}
