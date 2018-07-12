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

  val partitionChooser = style(
    filterChooser
  )

  val domainChooser = style(
    filterChooser
  )

  val additionalFiltersChooser = style(
    filterChooser
  )

  // legend

  val legendContainer = style()

  val legendTitle = style()
  val legendTitleText = style(
    fontWeight.bold
  )
  val legendTitleLinkText = style()

  val helpModal = style(
    addClassNames("modal", "fade")
  )
  val helpModalDialog = style(
    addClassNames("modal-dialog")
  )
  val helpModalContent = style(
    addClassNames("modal-content")
  )
  val helpModalHeader = style(
    addClassNames("modal-header")
  )
  val helpModalTitle = style(
    addClassNames("modal-title"),
    fontWeight.bold,
    fontSize(16 pt)
  )
  val helpModalHeaderCloseButton = style(
    addClassNames("close")
  )
  val helpModalBody = style(
    addClassNames("modal-body")
  )
  val helpModalFooter = style(
    addClassNames("modal-footer")
  )
  val helpModalFooterCloseButton = style(
    addClassNames("btn", "btn-secondary")
  )

  val validityLegend = style()

  val highlightLegend = style()
  val legendColorIndicator = style()


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

  val metadataLabelHeight = 1 rem
  val metadataLabelFontSize = 8 pt

  val metadataLabel = style(
    height(metadataLabelHeight),
    backgroundColor.white,
    fontSize(metadataLabelFontSize),
    verticalAlign.middle
  )
  val metadataLabelText = style(
    addClassNames("px-1"),
    whiteSpace.nowrap
  )
  val fixedPaneHeader = style(
    position.fixed,
    top(headerHeight)
  )

  val documentSelectionPaneWidth = 10 rem
  val sentenceSelectionPaneWidth = 12 rem

  val documentSelectionFontSize = 12 pt
  val sentenceSelectionFontSize = 10 pt

  val selectionPane = style(
    scrollPane,
    paddingTop(metadataLabelHeight),
    lineHeight(1.2)
  )

  val countLabel = style(
    metadataLabel,
    fixedPaneHeader,
    textAlign.right
  )
  val countLabelText = style(
    metadataLabelText
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
    fontSize(sentenceSelectionFontSize),
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
    addClassNames("px-3"),
    flex := "1"
  )

  val sentenceBox = style(
    fixedPaneHeader,
    backgroundColor.white
  )

  val sentenceInfoContainer = style(
    metadataLabel,
    textAlign.left
  )
  val sentenceInfoText = style(
    metadataLabelText
  )

  val sentenceTextContainer = style(
    addClassNames("pb-3"),
  )

  val verbEntriesContainer = style()

  val loadingNotice = style(
    addClassNames("p-3")
  )

  val sentenceText = style(
    fontSize(16 pt)
  )

  val verbEntryDisplay = style(
    addClassNames("p-2"),
    width(100 %%)
  )

  val verbHeading = style()
  val verbHeadingText = style(
    fontSize(16 pt),
    fontWeight.bold
  )
  val verbQAsTable = style(
    width(100 %%)
  )
  val verbQAsTableBody = style(
    width(100 %%)
  )
  val qaPairRow = style(
    addClassNames("p-1"),
    width(100 %%),
    &.nthChild("even")(
      backgroundColor(grey(240))
    )
  )
  val questionCell = style(
    width(12 rem)
  )
  val questionText = style()

  val validityCell = style(
    addClassNames("px-2"),
    width(2 rem)
  )
  val validityText = style()
  val validValidityText = style(
    validityText,
    color.green
  )
  val invalidValidityText = style(
    validityText,
    color.red
  )

  val answerCell = style()
  val answerText = style()

  val qaPairDisplay = style()
}
