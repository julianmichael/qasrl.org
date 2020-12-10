package qasrl
import sitegen.data.References
import sitegen.data.Authors

package object site {

  val publications = {
    import References._
    List(
      klein2020qanom,
      pyatkin2020qadiscourse,
      roit2020controlled,
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
      ayalKlein,
      paulRoit,
      valentinaPyatkin,
      gabiStanovsky,
      idoDagan,
      lukeZettlemoyer
    )
  }
}
