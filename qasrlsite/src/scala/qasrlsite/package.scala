import sitegen.data.References
import sitegen.data.Authors

package object qasrlsite {
  val publications = {
    import References._
    List(
      fitzgerald18qasrl,
      michael18qamr,
      stanovsky18supoie,
      he16hitl,
      he15qasrl
    )
  }

  val team = {
    import Authors._
    List(
      julianMichael,
      luhengHe,
      nicholasFitzgerald,
      gabiStanovsky,
      idoDagan,
      lukeZettlemoyer
    )
  }
}
