package qasrlbrowser

import cats.Id
import cats.data.NonEmptyList
import cats.implicits._

import scalajs.js
import org.scalajs.dom
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

import qasrl.bank.DataIndex
import qasrl.bank.DatasetPartition
import qasrl.bank.Document
import qasrl.bank.DocumentId
import qasrl.bank.DocumentMetadata
import qasrl.bank.Domain
import qasrl.bank.SentenceId

import qasrl.bank.service.{CacheCall, Cached, Remote}
import qasrl.bank.service.DocumentService

import qasrl.data.Sentence
import qasrl.data.VerbEntry
import qasrl.data.QuestionLabel

import nlpdata.util.Text
import nlpdata.util.LowerCaseStrings._

import scala.collection.immutable.SortedSet

object Browser {

  val IndexFetch = new CacheCallContentComponent[Unit, DataIndex]
  val DocMetaLocal = new LocalStateComponent[DocumentMetadata]
  val DocFetch = new CacheCallContentComponent[DocumentId, Document]
  val SentLocal = new LocalStateComponent[Sentence]

  case class Props(
    qasrl: DocumentService[CacheCall]
  )

  @Lenses case class Search(
    curText: String,
    keywords: Option[NonEmptyList[LowerCaseString]]
  )
  object Search {
    def initial = Search("", None)
  }

  @Lenses case class Filters(
    partitions: Set[DatasetPartition],
    domains: Set[Domain],
    denseOnly: Boolean
  )
  object Filters {
    def initial = Filters(
      Set(DatasetPartition.Dev),
      Set(Domain.Wikipedia),
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

  def curDocuments(index: DataIndex, filter: Filters) = {
    filter.partitions.iterator.map(
      part => index.documents(part).filter(doc => filter.domains.contains(doc.id.domain))
    ).reduce(_ union _)
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
        ^.value := search.get.curText,
        ^.onChange ==> ((e: ReactEventFromInput) => search.zoom(Search.curText).set(e.target.value)),
        ^.onKeyDown ==> (
          (e: ReactKeyboardEvent) => {
            println(e.keyCode)
            CallbackOption.keyCodeSwitch(e) {
              case KeyCode.Enter => search.zoom(Search.keywords).set(
                NonEmptyList.fromList(search.get.curText.split("\\s+").toList.map(_.lowerCase))
              )
            }
          }
        )
      ),
      <.button(S.searchClearButton)(
        ^.disabled := search.get.keywords.isEmpty,
        "Clear"
      )
      // s"Current query: ${search.get.keywords.fold("")(_.mkString(", "))}"
    )
  }

  def filterPane(filter: StateVal[Filters]) = {
    <.div(S.filterContainer)(
      // <.h5(S.filtersTitle)("Filters"),
      <.div(S.partitionChooser)(
        checkboxToggle("Train", filter.zoom(Filters.train)),
        checkboxToggle("Dev",   filter.zoom(Filters.dev)),
        // checkboxToggle("Test",  filter.zoom(Filters.test))
      ),
      <.div(S.domainChooser)(
        checkboxToggle("Wikipedia", filter.zoom(Filters.wikipedia)),
        checkboxToggle("Wikinews",  filter.zoom(Filters.wikinews)),
        checkboxToggle("TQA",       filter.zoom(Filters.tqa))
      ),
      <.div(S.denseOnlyChooser)(
        checkboxToggle("Dense only", filter.zoom(Filters.denseOnly))
      )
    )
  }

  def headerPane(state: StateVal[State]) = {
    <.div(S.headerContainer)(
      <.div(S.titleAndSearchContainer)(
        <.h1(S.title)("QA-SRL Bank 2.0"),
        searchPane(state.zoom(State.search))
      ),
      filterPane(state.zoom(State.filter))
    )
  }

  val S = BrowserStyles

  class Backend(scope: BackendScope[Props, State]) {

    def documentText(props: Props, doc: Document, setSentence: Sentence => Callback) = {
      <.p(
        doc.sentences.toVdomArray { sentence =>
          <.span(
            ^.key := sentence.sentenceId,
            ^.onClick --> setSentence(sentence),
            " " + Text.render(sentence.sentenceTokens) + " "
          )
        }
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

    def sentenceSelectionPane(
      document: Document,
      curSentence: StateVal[Sentence],
    ) = {
      <.div(S.scrollPane)(
        <.div(S.sentenceCountLabel)(
          <.span(S.sentenceCountLabelText)(
            s"${document.sentences.size} sentences"
          )
        ),
        <.div(S.sentenceSelectionPane)(
          document.sentences.toVdomArray { sentence =>
            <.div(S.sentenceSelectionEntry)(
              ^.key := sentence.sentenceId,
              if(sentence == curSentence.get) S.currentSelectionEntry else S.nonCurrentSelectionEntry,
              ^.onClick --> curSentence.set(sentence),
              <.span(S.sentenceSelectionEntryText)(
                Text.render(sentence.sentenceTokens)
              )
            )
          }
        )
      )
    }

    def verbEntryDisplay(
      verb: VerbEntry,
      curSentence: Sentence
    ) = {
      <.div(S.verbEntryDisplay)(
        ^.key := verb.verbIndex,
        <.div(S.verbHeading)(
          <.span(S.verbHeadingText)(
            curSentence.sentenceTokens(verb.verbIndex)
          )
        ),
        <.table(S.verbQAsTable)(
          <.tbody(S.verbQAsTableBody)(
            verb.questionLabels.toList.map(_._2).sortBy(_.questionString).toVdomArray { label =>
              qaLabelRow(label)
            }
          )
        )
      )
    }

    def qaLabelRow(label: QuestionLabel) = {
      <.tr(S.qaPairRow)(
        ^.key := label.questionString,
        <.td(S.questionCell)(
          <.span(S.questionText)(
            label.questionString
          )
        ),
        <.td(S.answerCell)(
          <.span(S.answerText)(
            "TODO answers"
          )
        )
      )
    }

    def sentenceDisplayPane(curSentence: Sentence) = {
      <.div(S.sentenceDisplayPane)(
        <.span(S.sentenceText)(
          Text.render(curSentence.sentenceTokens)
        ),
        curSentence.verbEntries.values.toList.sortBy(_.verbIndex).toVdomArray { verb =>
          verbEntryDisplay(verb, curSentence)
        }
      )
    }

    def render(props: Props, state: State) = {
      val stateVal = makeStateValForState(scope, state)
      <.div(S.mainContainer)(
        headerPane(stateVal),
        IndexFetch.make(request = (), sendRequest = _ => props.qasrl.getDataIndex) {
          case IndexFetch.Loading =>
            <.div(S.dataContainer)(
              <.span(S.loadingNotice)("Loading metadata...")
            )
          case IndexFetch.Loaded(index) =>
            val curDocMetas = curDocuments(index, state.filter)
            DocMetaLocal.make(
              initialValue = curDocMetas.head,
              shouldRefresh = _ => false
            ) { curDocMeta =>
              <.div(S.dataContainer)(
                docSelectionPane(index.numDocuments, curDocMetas, curDocMeta),
                DocFetch.make(request = curDocMeta.get.id, sendRequest = id => props.qasrl.getDocument(id)) {
                  case DocFetch.Loading =>
                    <.div(S.documentContainer)(
                      <.span(S.loadingNotice)("Loading document...")
                    )
                  case DocFetch.Loaded(doc) => SentLocal.make(initialValue = doc.sentences.head) { curSentence =>
                    <.div(S.documentContainer)(
                      sentenceSelectionPane(doc, curSentence),
                      sentenceDisplayPane(curSentence.get)
                    )
                  }
                }
              )
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
