import sitegen.data.References
import sitegen.data.Authors

import ammonite.ops.RelPath

package object qasrlsite {

  val browserScriptLocation = RelPath.empty / "scripts" / "browser.js"
  val browserScriptDepsLocation = RelPath.empty / "scripts" / "browser-deps.js"

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

  // TODO maybe a projects section at some point...
  // @sectionHeading("projects", "Projects"){Projects}
  //   @div(S.infoBox)
  //   @div(S.infoBoxBody)
  //   @h4{QA-SRL}
  //   @p
  // Introduced by
  //   @render.newciteAnchor(data.References.he15qasrl),
  // QA-SRL recasts semantic role labeling as a question-answer pair annotation task.
  //   @div(S.infoBox)
  //   @div(S.infoBoxBody)
  //   @h4{QAMR}
  //   @p
  // First introduced by
  //   @render.newciteAnchor(data.References.michael18qamr),
  // QAMR represents...
  //   @div(S.infoBox)
  //   @div(S.infoBoxBody)
  //   @h4{Open IE}
  //   @p
  // First introduced by
  // so-and-so,
  // QAMR represents...
}
