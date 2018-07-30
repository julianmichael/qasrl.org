import sitegen.data.References
import sitegen.data.Authors

package object qasrlsite {

  val publications = {
    import References._
    List(
      fitzGerald18qasrl,
      michael18qamr,
      stanovsky18supoie,
      he16hitl,
      stanovsky16benchmark,
      stanovsky16nonrestrictive,
      stanovsky16reduced,
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
      ayalKlein,
      idoDagan,
      lukeZettlemoyer
    )
  }
}
