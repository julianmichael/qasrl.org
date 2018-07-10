package qasrlbrowser

import scalacss.DevDefaults._
import scala.language.postfixOps

object BrowserStyles extends StyleSheet.Inline {
  import dsl._

  val checkbox = style(
    addClassNames("form-check-input")
  )

  val checkboxLabel = style(
    addClassNames("form-check-label")
  )

  val webkitScrollbar = {
    import scalacss.internal._
    Cond(Some(Pseudo.Custom("::-webkit-scrollbar", PseudoType.Element)), Vector.empty)
  }

  val mainContainer = style(
    addClassNames("container-fluid", "p-0", "m-0")
  )

  // header

  val headerHeight = 100 px

  val flexyHeaderThing = style(
    display.flex,
    // alignItems.center,
    // justifyContent.center,
    position.relative,
    zIndex(10)
  )

  val headerContainer = style(
    addClassNames("p-2"),
    flexyHeaderThing,
    height(headerHeight)
  )

  val titleAndSearchContainer = style()

  // title

  val title = style()

  // search

  val searchContainer = style(
    flexyHeaderThing,
    width(100 %%)
  )

  val searchInput = style()
  val searchClearButton = style()

  // filters

  val filterContainer = style(
    addClassNames("px-4"),
    flexyHeaderThing
  )

  val filterChooser = style(
    addClassNames("form-check", "px-3")
  )

  val partitionTitle = style()
  val partitionChooser = style(
    filterChooser
  )

  val domainTitle = style()
  val domainChooser = style(
    filterChooser
  )

  val denseOnlyTitle = style()
  val denseOnlyChooser = style(
    filterChooser
  )

  // main data display

  val dataContainer = style(
    position.relative,
    overflow.hidden,
    backfaceVisibility.hidden,
    willChange := "overflow",
    display.flex,
    height(100 vh),
    marginTop(-headerHeight),
    paddingTop(headerHeight),
    width(100 %%)
  )

  val scrollPane = style(
    overflow.auto,
    height.auto,
    webkitScrollbar(
      display.none
    )
    // attr("-webkit-overflow-scrolling") := "touch",
    // attr("-ms-overflow-style") := "none"
  )

  // selection of sentences

  val countLabelHeight = 1 rem
  val countLabelFontSize = 8 pt

  val documentSelectionPaneWidth = 10 rem
  val sentenceSelectionPaneWidth = 12 rem

  val documentSelectionFontSize = 12 pt
  val sentenceSelectionFontSize = 10 pt

  val selectionPane = style(
    scrollPane,
    paddingTop(countLabelHeight),
  )

  val countLabel = style(
    position.fixed,
    top(headerHeight),
    height(countLabelHeight),
    backgroundColor.white,
    fontSize(countLabelFontSize),
    textAlign.right,
    verticalAlign.middle
  )
  val countLabelText = style(
    addClassNames("px-1")
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

  val documentCountLabel = style(
    countLabel,
    width(documentSelectionPaneWidth)
  )

  val documentCountLabelText = style(
    countLabelText
  )

  val documentSelectionPane = style(
    selectionPane,
    width(documentSelectionPaneWidth)
  )

  val documentSelectionEntry = style(
    selectionEntry
  )

  val documentSelectionEntryText = style(
    fontSize(documentSelectionFontSize)
  )

  val sentenceCountLabel = style(
    countLabel,
    left(documentSelectionPaneWidth),
    width(sentenceSelectionPaneWidth)
  )

  val sentenceCountLabelText = style(
    countLabelText
  )

  val sentenceSelectionPane = style(
    selectionPane,
    width(sentenceSelectionPaneWidth)
  )

  val sentenceSelectionEntry = style(
    selectionEntry
  )

  val sentenceSelectionEntryText = style(
    fontSize(sentenceSelectionFontSize)
  )

  // display of document biggy thing

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
    scrollPane,
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
