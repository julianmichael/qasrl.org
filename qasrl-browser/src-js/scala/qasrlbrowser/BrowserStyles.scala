package qasrlbrowser

import scalacss.DevDefaults._
import scala.language.postfixOps

object BrowserStyles extends StyleSheet.Inline {
  import dsl._

  val webkitScrollbar = {
    import scalacss.internal._
    Cond(Some(Pseudo.Custom("::-webkit-scrollbar", PseudoType.Element)), Vector.empty)
  }

  val mainContainer = style(
    addClassNames("container-fluid", "p-0", "m-0")
  )

  // settings

  val settingsHeight = 100 px

  val settingsContainer = style(
    display.flex,
    alignItems.center,
    justifyContent.center,
    position.relative,
    zIndex(10),
    height(settingsHeight)
  )

  val partitionChooser = style()
  val domainChooser = style()
  val denseOnlyToggle = style()

  // main data display

  val dataContainer = style(
    display.flex,
    overflow.hidden,
    height(100 vh),
    marginTop(-settingsHeight),
    paddingTop(settingsHeight),
    position.relative,
    width(100 %%),
    backfaceVisibility.hidden,
    willChange := "overflow"
  )

  val dataPane = style(
    overflow.auto,
    height.auto,
    webkitScrollbar(
      display.none
    )
    // attr("-webkit-overflow-scrolling") := "touch",
    // attr("-ms-overflow-style") := "none"
  )

  // selection of sentences

  val selectionPane = style(
    dataPane,
  )

  val documentSelectionPane = style(
    selectionPane,
    width(10 rem)
  )

  val sentenceSelectionPane = style(
    selectionPane,
    width(12 rem),
    fontSize(10 pt)
  )

  val selectionEntry = style(
    addClassNames("p-2"),
    &.hover(
      backgroundColor(grey(220))
    )
  )

  val currentSelectionEntry = style(
    selectionEntry,
    backgroundColor(grey(200))
  )

  val nonCurrentSelectionEntry = style(
    selectionEntry
  )

  val selectionEntryText = style(
    fontSize(12 pt)
  )

  val documentContainer = style(
    flex := "1",
    display.flex,
    overflow.hidden,
    position.relative,
    backfaceVisibility.hidden,
    willChange := "overflow"
  )

  // display of sentence data

  val sentenceDisplayPane = style(
    fontSize(12 pt),
    dataPane,
    addClassNames("p-3"),
    flex := "1"
  )

  val loadingNotice = style()

  val sentenceText = style(
    fontSize(16 pt)
  )

  val verbEntryDisplay = style(
    addClassNames("p-1")
  )

  val verbHeading = style()
  val verbHeadingText = style(
    fontWeight.bold
  )
  val verbQAsTable = style()
  val verbQAsTableBody = style()
  val qaPairRow = style(
    addClassNames("p-1"),
    &.nthChild("even")(
      backgroundColor(grey(240))
    )
  )
  val questionCell = style(
    width(25 rem)
  )
  val questionText = style()
  val answerCell = style()
  val answerText = style()

  val qaPairDisplay = style()
}
