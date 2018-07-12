package qasrlbrowser

import cats.Id
import cats.Order
import cats.data.NonEmptyList
import cats.implicits._

import scalajs.js
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.ext.KeyCode

import scala.concurrent.ExecutionContext.Implicits.global

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

import scalacss.DevDefaults._
import scalacss.ScalaCssReact._

import monocle._
import monocle.function.{all => Optics}
import monocle.macros._
import japgolly.scalajs.react.MonocleReact._

import qasrl.bank.AnswerSource
import qasrl.bank.AnnotationRound
import qasrl.bank.DataIndex
import qasrl.bank.DatasetPartition
import qasrl.bank.Document
import qasrl.bank.DocumentId
import qasrl.bank.DocumentMetadata
import qasrl.bank.Domain
import qasrl.bank.QuestionSource
import qasrl.bank.SentenceId

import qasrl.bank.service.{CacheCall, Cached, Remote}
import qasrl.bank.service.DocumentService

import qasrl.data.AnswerLabel
import qasrl.data.AnswerJudgment
import qasrl.data.AnswerSpan
import qasrl.data.Answer
import qasrl.data.InvalidQuestion
import qasrl.data.Sentence
import qasrl.data.VerbEntry
import qasrl.data.QuestionLabel

import nlpdata.util.Text
import nlpdata.util.LowerCaseStrings._

import scala.collection.immutable.SortedSet

object Browser {

  val IndexFetch = new CacheCallContentComponent[Unit, DataIndex]
  val SearchFetch = new CacheCallContentComponent[Set[LowerCaseString], Set[DocumentId]]
  val DocMetaLocal = new LocalStateComponent[DocumentMetadata]
  val DocFetch = new CacheCallContentComponent[DocumentId, Document]
  val SentLocal = new LocalStateComponent[Sentence]
  val DivReference = new ReferenceComponent[html.Div]
  val BoolLocal = new LocalStateComponent[Boolean]

  case class Props(
    qasrl: DocumentService[CacheCall]
  )

  @Lenses case class Search(
    text: String,
    query: Set[LowerCaseString]
  )
  object Search {
    def initial = Search("", Set.empty[LowerCaseString])
  }

  @Lenses case class Slices(
    original: Boolean,
    expansion: Boolean,
    eval: Boolean
  )
  object Slices {
    val initial = Slices(true, true, true)
  }

  @Lenses case class Filters(
    partitions: Set[DatasetPartition],
    domains: Set[Domain],
    slices: Slices,
    validOnly: Boolean
  )
  object Filters {
    def initial = Filters(
      Set(DatasetPartition.Dev),
      Set(Domain.Wikipedia),
      Slices.initial,
      false
    )
  }
  implicit class FiltersLenses(val fs: Filters.type) extends AnyVal {
    def atPart(part: DatasetPartition) = Optics.at(part)(monocle.function.At.atSet)
    def train     = fs.partitions composeLens atPart(DatasetPartition.Train)
    def dev       = fs.partitions composeLens atPart(DatasetPartition.Dev)
    def test      = fs.partitions composeLens atPart(DatasetPartition.Test)

    def atDomain(domain: Domain) = Optics.at(domain)(monocle.function.At.atSet)
    def wikipedia = fs.domains composeLens atDomain(Domain.Wikipedia)
    def wikinews  = fs.domains composeLens atDomain(Domain.Wikinews)
    def tqa       = fs.domains composeLens atDomain(Domain.TQA)
  }

  def getCurDocuments(
    index: DataIndex,
    searchedIds: Set[DocumentId],
    filter: Filters,
    denseIds: Set[DocumentId],
  ) = {
    filter.partitions.iterator.map(
      part => index.documents(part)
        .filter(doc =>
        filter.domains.contains(doc.id.domain) &&
          searchedIds.contains(doc.id) &&
          (filter.slices.original || filter.slices.expansion || (filter.slices.eval && denseIds.contains(doc.id)))
      )
    ).foldLeft(SortedSet.empty[DocumentMetadata])(_ union _)
  }

  @Lenses case class State(
    search: Search,
    filter: Filters
  )
  object State {
    def initial(props: Props) = {
      val search = Search.initial
      val filter = Filters.initial
      State(search, filter)
    }
  }

  def makeStateValForState[P, S](
    scope: BackendScope[P, S],
    state: S
  ) = StateVal[S](state, s => scope.setState(s))

  val transparent = Rgba(255, 255, 255, 0.0)
  val answerHighlightLayer = Rgba(255, 255, 0, 0.2)
  val queryKeywordHighlightLayer = Rgba(255, 255, 0, 0.4)

  val highlightLayerColors = List(
    Rgba(255, 255,   0, 0.2), // yellow
    Rgba(  0, 128, 255, 0.2), // green-blue
    Rgba(255,   0, 128, 0.2), // magenta?
    Rgba(128, 255,   0, 0.2), // something. idk
    Rgba(128,   0, 255, 0.2), //
    Rgba(  0, 255, 128, 0.2)  //
  )

  def checkboxToggle[A](
    label: String,
    isValueActive: StateVal[Boolean]
  ) = <.div(
    <.input(S.checkbox)(
      ^.`type` := "checkbox",
      ^.value := label,
      ^.checked := isValueActive.get,
      ^.onChange --> isValueActive.modify(!_)
    ),
    <.span(S.checkboxLabel)(
      label
    )
  )

  def searchPane(search: StateVal[Search]) = {
    <.div(S.searchContainer)(
      <.input(S.searchInput)(
        ^.`type` := "text",
        ^.placeholder := "Keyword search",
        ^.value := search.get.text,
        ^.onChange ==> ((e: ReactEventFromInput) => search.zoom(Search.text).set(e.target.value)),
        ^.onKeyDown ==> (
          (e: ReactKeyboardEvent) => {
            val query = search.get.text.split("\\s+")
              .map(_.trim).toSet
              .filter(_.nonEmpty)
              .map((s: String) => s.lowerCase)
            CallbackOption.keyCodeSwitch(e) {
              case KeyCode.Enter => search.zoom(Search.query).set(query)
            }
          }
        )
      ),
      <.button(S.searchClearButton)(
        ^.disabled := search.get.query.isEmpty,
        ^.onClick --> search.zoom(Search.query).set(Set.empty[LowerCaseString]),
        "Clear"
      )
      // s"Current query: ${search.get.keywords.fold("")(_.mkString(", "))}"
    )
  }

  def filterPane(filter: StateVal[Filters]) = {
    val slices = filter.zoom(Filters.slices)
    <.div(S.filterContainer)(
      <.div(S.partitionChooser)(
        checkboxToggle("Train", filter.zoom(Filters.train)),
        checkboxToggle("Dev",   filter.zoom(Filters.dev)),
        checkboxToggle("Test",  filter.zoom(Filters.test))(^.visibility := "hidden")
      ),
      <.div(S.domainChooser)(
        checkboxToggle("Wikipedia", filter.zoom(Filters.wikipedia)),
        checkboxToggle("Wikinews",  filter.zoom(Filters.wikinews)),
        checkboxToggle("TQA",       filter.zoom(Filters.tqa))
      ),
      <.div(S.sliceChooser)(
        checkboxToggle("Original",  slices.zoom(Slices.original)),
        checkboxToggle("Expansion", slices.zoom(Slices.expansion)),
        checkboxToggle("Eval",      slices.zoom(Slices.eval))
      )
    )
  }

  val helpModalId = "help-modal"
  val helpModalLabelId = "help-modal-label"
  val dataToggle = VdomAttr("data-toggle")
  val dataTarget = VdomAttr("data-target")
  val ariaLabelledBy = VdomAttr("aria-labelledby")
  val ariaHidden = VdomAttr("aria-hidden")
  val dataDismiss = VdomAttr("data-dismiss")
  val ariaLabel = VdomAttr("aria-label")

  def helpModal = {
    <.div(^.id := helpModalId)(
      S.helpModal, ^.tabIndex := -1, ^.role := "dialog",
      ariaLabelledBy := helpModalLabelId, ariaHidden := true
    )(
      <.div(S.helpModalDialog, ^.role := "document")(
        <.div(S.helpModalContent)(
          <.div(S.helpModalHeader)(
            <.span(S.helpModalTitle)(
              ^.id := helpModalLabelId,
              "Full legend"
            ),
            <.button(S.helpModalHeaderCloseButton)(
              ^.`type` := "button", dataDismiss := "modal", ariaLabel := "Close",
              <.span(ariaHidden := true, "×")
            )
          ),
          <.div(S.helpModalBody)(
            "Full legend description is under construction."
          ),
          <.div(S.helpModalFooter)(
            <.button(S.helpModalFooterCloseButton)(
              ^.`type` := "button", dataDismiss := "modal")(
              "Close"
            )
          )
        )
      )
    )
  }

  def legendPane(validOnly: StateVal[Boolean]) = {
    <.div(S.legendContainer)(
      <.div(S.legendTitle)(
        <.span(S.legendTitleText)("Legend "),
        <.span(S.legendTitleLinkText)(
          <.a(
            ^.href := "#", "(help)",
            dataToggle := "modal",
            dataTarget := s"#$helpModalId"
          )
        ),
        ": ",
        <.div(S.originalLegendMark)("m"),
        <.span(" Original "),
        <.div(S.expansionLegendMark)("m"),
        <.span(" Expansion "),
        <.div(S.evalLegendMark)("m"),
        <.span(" Eval")
      ),
      <.div(S.validityLegend)(
        checkboxToggle("Valid only ", validOnly)(
          ^.marginLeft := "20px", ^.display := "inline"
        ),
        <.span("("),
        <.span(S.invalidValidityText)("≤ 4/6 ➔ invalid"),
        <.span(", "),
        <.span(S.validValidityText)("≥ 5/6 ➔ valid"),
        <.span(")")
      ),
      <.div(S.highlightLegend)(
        <.span("Answer provided by "),
        (1 to 6).flatMap { i =>
          val colorStr = NonEmptyList(
            transparent,
            List.fill(i)(answerHighlightLayer)
          ).reduce((x: Rgba, y: Rgba) => x add y).toColorStyleString
          List(
            <.span(^.key := s"slashafterlegend-$i", "/"),
            <.span(S.legendColorIndicator)(
              ^.key := s"legendnum-$i",
              ^.style := js.Dynamic.literal("backgroundColor" -> colorStr),
              f"$i%d"
            )
          )
        }.tail.toVdomArray(x => x),
        <.span(" annotators")
      )
    )
  }

  def headerPane(state: StateVal[State]) = {
    <.div(S.headerContainer)(
      <.div(S.titleAndSearchContainer)(
        <.h1(S.title)("QA-SRL Bank 2.0"),
        searchPane(state.zoom(State.search))
      ),
      filterPane(state.zoom(State.filter)),
      legendPane(state.zoom(State.filter).zoom(Filters.validOnly))
    )
  }

  def getCurSentences(
    allSentences: SortedSet[Sentence],
    query: Set[LowerCaseString],
    denseIds: Set[SentenceId],
    slices: Slices
  ) = {
    val searchFilteredSentences = if(query.isEmpty) {
      allSentences
    } else {
      allSentences.filter { sent =>
        val sentTokenSet = sent.sentenceTokens
          .flatMap(t => List(t, Text.normalizeToken(t)))
          .map(_.lowerCase)
          .toSet
        sentTokenSet.intersect(query).nonEmpty
      }
    }
    val sliceFilteredSentences = allSentences.filter { sent =>
      slices.original || slices.expansion || (slices.eval && denseIds.contains(SentenceId.fromString(sent.sentenceId)))
    }
    searchFilteredSentences.intersect(sliceFilteredSentences)
  }

  def getRoundForQuestion(label: QuestionLabel) = {
    val qSource = label.questionSources.map(s => QuestionSource.fromString(s): QuestionSource).min
    qSource match {
      case QuestionSource.Turker(_) => AnnotationRound.Original
      case QuestionSource.Model(_)  =>
        val hasAnswersInExpansion = label.answerJudgments.map(_.sourceId).exists(s =>
          AnswerSource.fromString(s).round == AnnotationRound.Expansion
        )
        if(hasAnswersInExpansion) AnnotationRound.Expansion else AnnotationRound.Eval
    }
  }

  import qasrl.bank.RichOrderObject
  import cats.Order.catsKernelOrderingForOrder

  implicit val answerSpanOrder: Order[AnswerSpan] = Order.whenEqual(
    Order.by[AnswerSpan, Int](_.begin),
    Order.by[AnswerSpan, Int](_.end)
  )
  implicit val qasrlDataQuestionLabelOrder: Order[QuestionLabel] = Order.whenEqual(
    Order.by[QuestionLabel, AnnotationRound](getRoundForQuestion _),
    Order.by[QuestionLabel, String](_.questionString)
  )

  def spanOverlaps(x: AnswerSpan, y: AnswerSpan): Boolean = {
    x.begin < y.end && y.begin < x.end
  }
  def spanContains(s: AnswerSpan, q: Int): Boolean = {
    q >= s.begin && q < s.end
  }

  sealed trait SpanColoringSpec {
    def spansWithColors: List[(AnswerSpan, Rgba)]
  }
  case class RenderWholeSentence(val spansWithColors: List[(AnswerSpan, Rgba)]) extends SpanColoringSpec
  case class RenderRelevantPortion(spansWithColorsNel: NonEmptyList[(AnswerSpan, Rgba)]) extends SpanColoringSpec {
    def spansWithColors = spansWithColorsNel.toList
  }

  def renderSentenceWithHighlights(
    sentenceTokens: Vector[String],
    coloringSpec: SpanColoringSpec
  ) = {
    val containingSpan = coloringSpec match {
      case RenderWholeSentence(_) =>
        AnswerSpan(0, sentenceTokens.size)
      case RenderRelevantPortion(swcNel) =>
        val spans = swcNel.map(_._1)
        AnswerSpan(spans.map(_.begin).minimum, spans.map(_.end).maximum)
    }
    val wordIndexToLayeredColors = (containingSpan.begin until containingSpan.end).map { i =>
      i -> coloringSpec.spansWithColors.collect {
        case (span, color) if spanContains(span, i) => color
      }
    }.toMap
    val indexAfterToSpaceLayeredColors = ((containingSpan.begin + 1) to containingSpan.end).map { i =>
      i -> coloringSpec.spansWithColors.collect {
        case (span, color) if spanContains(span, i - 1) && spanContains(span, i) => color
      }
    }.toMap
    Text.render[Int, List, List[VdomElement]](
      words = sentenceTokens.indices.toList,
      getToken = (index: Int) => sentenceTokens(index),
      spaceFromNextWord = (nextIndex: Int) => {
        if(!spanContains(containingSpan, nextIndex) || nextIndex == containingSpan.begin) List() else {
          val colors = indexAfterToSpaceLayeredColors(nextIndex)
          val colorStr = NonEmptyList[Rgba](transparent, colors)
            .reduce((x: Rgba, y: Rgba) => x add y).toColorStyleString
          List(
            <.span(
              ^.key := s"space-$nextIndex",
              ^.style := js.Dynamic.literal("backgroundColor" -> colorStr),
              " "
            )
          )
        }
      },
      renderWord = (index: Int) => {
        if(!spanContains(containingSpan, index)) List() else {
          val colorStr = NonEmptyList(transparent, wordIndexToLayeredColors(index))
            .reduce((x: Rgba, y: Rgba) => x add y).toColorStyleString
          List(
            <.span(
              ^.key := s"word-$index",
              ^.style := js.Dynamic.literal("backgroundColor" -> colorStr),
              Text.normalizeToken(sentenceTokens(index))
            )
          )
        }
      }
    ).toVdomArray(x => x)
  }

  def makeAllHighlightedAnswer(
    sentenceTokens: Vector[String],
    answers: NonEmptyList[Answer]
  ): VdomArray = {
    val orderedSpans = answers.flatMap(a => NonEmptyList.fromList(a.spans.toList).get).sorted
    case class GroupingState(
      completeGroups: List[NonEmptyList[AnswerSpan]],
      currentGroup: NonEmptyList[AnswerSpan]
    )
    val groupingState = orderedSpans.tail.foldLeft(GroupingState(Nil, NonEmptyList.of(orderedSpans.head))) {
      case (GroupingState(groups, curGroup), span) =>
        if(curGroup.exists(s => spanOverlaps(s, span))) {
          GroupingState(groups, span :: curGroup)
        } else {
          GroupingState(curGroup :: groups, NonEmptyList.of(span))
        }
    }
    val contigSpanLists = NonEmptyList(groupingState.currentGroup, groupingState.completeGroups)
    val answerHighlighties = contigSpanLists.reverse.map(spanList =>
      List(
        <.span(
          renderSentenceWithHighlights(sentenceTokens, RenderRelevantPortion(spanList.map(_ -> answerHighlightLayer)))
        )
      )
    ).intercalate(List(<.span(" / ")))
    answerHighlighties.zipWithIndex.toVdomArray { case (a, i) =>
      a(^.key := s"answerString-$i")
    }
  }

  def shouldAnswerBeIncluded(
    source: AnswerSource,
    slices: Slices
  ): Boolean = {
    // show answers from other rounds as well when looking at eval round
    import AnnotationRound._
    source.round match {
      case Original => slices.original || slices.eval
      case Expansion => slices.expansion || slices.eval
      case Eval => slices.eval
    }
  }

  def isQuestionValid(
    label: QuestionLabel,
    slices: Slices
  ): Boolean = {
    val includedJudgments = label.answerJudgments.filter(aj =>
      shouldAnswerBeIncluded(AnswerSource.fromString(aj.sourceId), slices)
    )
    val numValidJudgments = includedJudgments.count(_.judgment.isAnswer)
    numValidJudgments.toDouble / includedJudgments.size > (4.99 / 6.0)
  }

  def qaLabelRow(
    sentence: Sentence,
    label: QuestionLabel,
    slices: Slices
  ) = {
    val answerJudgments = label.answerJudgments.filter { aj =>
      shouldAnswerBeIncluded(AnswerSource.fromString(aj.sourceId), slices)
    }
    val qSource = label.questionSources.map(s => QuestionSource.fromString(s): QuestionSource).min
    val roundIndicatorStyle = qSource match {
      case QuestionSource.Turker(_) => S.originalRoundIndicator
      case QuestionSource.Model(_)  =>
        val hasAnswersInExpansion = label.answerJudgments.map(_.sourceId).exists(s =>
          AnswerSource.fromString(s).round == AnnotationRound.Expansion
        )
        if(hasAnswersInExpansion) S.expansionRoundIndicator else S.evalRoundIndicator
    }
    <.tr(S.qaPairRow)(
      <.td(roundIndicatorStyle),
      <.td(S.questionCell)(
        <.span(S.questionText)(
          label.questionString
        )
      ),
      <.td(S.validityCell) {
        val numJudgments = answerJudgments.size
        val numValidJudgments = answerJudgments.count(_.judgment.isAnswer)
        val isConsideredValid = isQuestionValid(label, slices)
        <.span(if(isConsideredValid) S.validValidityText else S.invalidValidityText)(
          s"$numValidJudgments/$numJudgments"
        )
      },
      <.td(S.answerCell)(
        <.span(S.answerText) {
          NonEmptyList.fromList(
            answerJudgments.toList.collect {
              case AnswerLabel(sourceId, Answer(spans)) => Answer(spans)
            }
          ).whenDefined { answersNel =>
            makeAllHighlightedAnswer(sentence.sentenceTokens, answersNel)
          }
        }
      )
    )
  }

  def shouldQuestionBeShown(
    label: QuestionLabel,
    slices: Slices,
    validOnly: Boolean
  ): Boolean = {
    (!validOnly || isQuestionValid(label, slices)) && {
      import AnnotationRound._
      val hasEvalAnswers = label.answerJudgments.map(aj => AnswerSource.fromString(aj.sourceId).round).contains(Eval)
      (hasEvalAnswers && slices.eval) || (
        getRoundForQuestion(label) match {
          case Original  => slices.original
          case Expansion => slices.expansion
          case Eval      => slices.eval
        }
      )
    }
  }

  def verbEntryDisplay(
    curSentence: Sentence,
    verb: VerbEntry,
    slices: Slices,
    validOnly: Boolean
  ) = {
    <.div(S.verbEntryDisplay)(
      <.div(S.verbHeading)(
        <.span(S.verbHeadingText)(
          curSentence.sentenceTokens(verb.verbIndex)
        )
      ),
      <.table(S.verbQAsTable)(
        <.tbody(S.verbQAsTableBody){
          val questionLabels = verb.questionLabels.toList.map(_._2)
            .filter(l => shouldQuestionBeShown(l, slices, validOnly))
            .sorted
          if(questionLabels.isEmpty) {
            <.span(S.loadingNotice)("All questions have been filtered out.")
          } else questionLabels
            .toVdomArray { label =>
            qaLabelRow(curSentence, label, slices)(^.key := label.questionString)
          }
        }
      )
    )
  }

  def sentenceDisplayPane(
    part: DatasetPartition,
    docMeta: DocumentMetadata,
    sentence: Sentence,
    slices: Slices,
    validOnly: Boolean
  ) = {
    val sentenceId = SentenceId.fromString(sentence.sentenceId)
    // TODO color differently depending on verb; add verb colors
    val answerSpansWithColors = for {
      verb <- sentence.verbEntries.values.toList
      question <- verb.questionLabels.values.toList
      if shouldQuestionBeShown(question, slices, validOnly)
      answerLabel <- question.answerJudgments
      if shouldAnswerBeIncluded(AnswerSource.fromString(answerLabel.sourceId), slices)
      Answer(spans) <- answerLabel.judgment.getAnswer.toList
      span <- spans.toList
    } yield span -> answerHighlightLayer
    DivReference.make(
      <.div(S.sentenceBox)(
        <.div(S.sentenceInfoContainer)(
          <.span(S.sentenceInfoText) {
            val abbrevTitle = if(docMeta.title.length <= 30) docMeta.title else docMeta.title.take(27) + "..."
            s"$part / ${docMeta.id.domain} / ${docMeta.id.id} ($abbrevTitle) / paragraph ${sentenceId.paragraphNum}, sentence ${sentenceId.sentenceNum}"
          }
        ),
        <.div(S.sentenceTextContainer)(
          <.span(S.sentenceText)(
            renderSentenceWithHighlights(sentence.sentenceTokens, RenderWholeSentence(answerSpansWithColors))
          )
        )
      )
    ) { case (sentBox, sentBoxRefOpt) =>
        <.div(S.sentenceDisplayPane)(
          sentBox,
          sentBoxRefOpt.fold(<.div()) { sentenceBoxRef =>
            val rect = sentenceBoxRef.getBoundingClientRect
            val height = math.round(rect.height)
              <.div(S.verbEntriesContainer)(
                ^.paddingTop := s"${height}px",
                sentence.verbEntries.values.toList.sortBy(_.verbIndex).toVdomArray { verb =>
                  verbEntryDisplay(sentence, verb, slices, validOnly)(^.key := verb.verbIndex)
                }
              )
          }
        )
    }
  }

  def sentenceSelectionPane(
    numSentencesInDocument: Int,
    curSentences: SortedSet[Sentence],
    searchQuery: Set[LowerCaseString],
    curSentence: StateVal[Sentence]
  ) = {
    val sentenceCountLabel = if(curSentences.size == numSentencesInDocument) {
      s"$numSentencesInDocument sentences"
    } else {
      s"${curSentences.size} / $numSentencesInDocument sentences"
    }

    <.div(S.scrollPane)(
      <.div(S.sentenceCountLabel)(
        <.span(S.sentenceCountLabelText)(
          sentenceCountLabel
        )
      ),
      <.div(S.sentenceSelectionPane)(
        curSentences.toVdomArray { sentence =>
          val spanHighlights = qasrl.bank.service.getQueryMatchesInSentence(sentence, searchQuery).toList.map(index =>
            AnswerSpan(index, index + 1) -> queryKeywordHighlightLayer
          )
          <.div(S.sentenceSelectionEntry)(
            ^.key := sentence.sentenceId,
            if(sentence == curSentence.get) S.currentSelectionEntry else S.nonCurrentSelectionEntry,
            ^.onClick --> curSentence.set(sentence),
            <.span(S.sentenceSelectionEntryText)(
              renderSentenceWithHighlights(sentence.sentenceTokens, RenderWholeSentence(spanHighlights))
            )
          )
        }
      )
    )
  }

  def docSelectionPane(
    totalNumDocs: Int,
    curDocMetas: SortedSet[DocumentMetadata],
    curDocMeta: StateVal[DocumentMetadata]
  ) = {
    <.div(
      S.scrollPane,
      <.div(S.documentCountLabel)(
        <.span(S.documentCountLabelText)(
          s"${curDocMetas.size} / $totalNumDocs documents"
        )
      ),
      <.div(S.documentSelectionPane)(
        curDocMetas.toVdomArray { docMeta =>
          <.div(S.documentSelectionEntry)(
            ^.key := docMeta.id.toString,
            if(docMeta == curDocMeta.get) S.currentSelectionEntry else S.nonCurrentSelectionEntry,
            ^.onClick --> curDocMeta.set(docMeta),
            <.span(S.documentSelectionEntryText)(
              docMeta.title
            )
          )
        }
      )
    )
  }

  val S = BrowserStyles

  class Backend(scope: BackendScope[Props, State]) {

    def render(props: Props, state: State) = {
      val stateVal = makeStateValForState(scope, state)
      <.div(S.mainContainer)(
        helpModal,
        headerPane(stateVal),
        IndexFetch.make(request = (), sendRequest = _ => props.qasrl.getDataIndex) {
          case IndexFetch.Loading =>
            <.div(S.dataContainer)(
              <.span(S.loadingNotice)("Loading metadata...")
            )
          case IndexFetch.Loaded(index) =>
            SearchFetch.make(request = state.search.query, sendRequest = props.qasrl.searchDocuments _) {
              case SearchFetch.Loading =>
                <.span(S.loadingNotice)("Waiting for search results...")
              case SearchFetch.Loaded(searchResultIds) =>
                val denseDocIds = index.denseIds.map(_.documentId)
                val curDocMetas = getCurDocuments(index, searchResultIds, state.filter, denseDocIds)
                if(curDocMetas.isEmpty) {
                  <.span(S.loadingNotice)("All documents have been filtered out.")
                } else DocMetaLocal.make(
                  initialValue = curDocMetas.head,
                  shouldRefresh = _ => false
                ) { curDocMeta =>
                  <.div(S.dataContainer)(
                    docSelectionPane(
                      index.numDocuments,
                      curDocMetas,
                      curDocMeta
                    ),
                    DocFetch.make(request = curDocMeta.get.id, sendRequest = id => props.qasrl.getDocument(id)) {
                      case DocFetch.Loading =>
                        <.div(S.documentContainer)(
                          <.span(S.loadingNotice)("Loading document...")
                        )
                      case DocFetch.Loaded(doc) =>
                        val curSentences = getCurSentences(doc.sentences, state.search.query, index.denseIds, state.filter.slices)
                        if(curSentences.isEmpty) {
                          <.div(
                            <.div(<.span(S.loadingNotice)("Current document: " + doc.metadata.title)),
                            <.div(<.span(S.loadingNotice)("All sentences have been filtered out."))
                          )
                        } else SentLocal.make(initialValue = curSentences.head) { curSentence =>
                          <.div(S.documentContainer)(
                            sentenceSelectionPane(
                              doc.sentences.size,
                              curSentences,
                              state.search.query,
                              curSentence,
                            ),
                            sentenceDisplayPane(
                              index.getPart(curDocMeta.get.id),
                              curDocMeta.get,
                              curSentence.get,
                              state.filter.slices,
                              state.filter.validOnly
                            )
                          )
                        }
                    }
                  )
                }
            }
        }
      )
    }
  }

  val Component = ScalaComponent.builder[Props]("Browser")
    .initialStateFromProps((props: Props) => State.initial(props))
    .renderBackend[Backend]
    .build
}
