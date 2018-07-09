package qasrlbrowser

import cats.Id
import cats.implicits._

import scalajs.js
import org.scalajs.dom
// import org.scalajs.dom.raw._
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

object BrowserComponent {

  val IndexFetch = new CacheCallContentComponent[Unit, DataIndex]
  val DocMetaLocal = new LocalStateComponent[DocumentMetadata]
  val DocFetch = new CacheCallContentComponent[DocumentId, Document]
  val SentLocal = new LocalStateComponent[Sentence]

  case class BrowserProps(
    qasrl: DocumentService[CacheCall]
  )

  @Lenses case class FilterState(
    partition: DatasetPartition,
    domain: Domain,
    denseOnly: Boolean
  )
  object FilterState {
    def initial = FilterState(
      DatasetPartition.Dev,
      Domain.Wikipedia,
      false
    )
  }

  def curDocuments(index: DataIndex, filter: FilterState) = {
    index.documents(filter.partition).filter(_.id.domain == filter.domain)
  }

  // @Lenses case class DocumentState(
  //   curDoc: Document,
  //   curSentence: Option[Sentence]
  // )

  @Lenses case class BrowserState(
    filter: FilterState
    // curDocMeta: Option[DocumentMetadata],
    // currentHighlightedVerbIndex: Option[Int],
    // currentHighlightedQAIndex: Option[Int],
    // currentId: SentenceMetadata,
    // currentSentence: Option[Sentence],
    // currentFilters: Map[String, Set[String]]
  )
  object BrowserState {
    def initial(props: BrowserProps) = {
      val filter = FilterState.initial
      BrowserState(filter)
    }
  }

  val S = BrowserStyles

  class BrowserBackend(scope: BackendScope[BrowserProps, BrowserState]) {

    def partitionChoice(state: BrowserState, label: String, partition: DatasetPartition) =
      <.div(
        <.input(
          ^.`type` := "radio",
          ^.name := "partition",
          ^.value := label,
          ^.checked := state.filter.partition == partition,
          ^.onClick --> scope.modState(
            (BrowserState.filter
               composeLens FilterState.partition
            ).set(partition)
          )
        ), label
      )

    def domainChoice(state: BrowserState, label: String, domain: Domain) =
      <.div(
        <.input(
          ^.`type` := "radio",
          ^.name := "domain",
          ^.value := label,
          ^.checked := state.filter.domain == domain,
          ^.onClick --> scope.modState(
            (BrowserState.filter
               composeLens FilterState.domain
            ).set(domain)
          )
        ), label
      )

    def denseOnlyCheckbox(state: BrowserState) =
      <.div(
        <.input(
          ^.`type` := "checkbox",
          ^.checked := state.filter.denseOnly,
          ^.onChange --> scope.modState(
            (BrowserState.filter
               composeLens FilterState.denseOnly
            ).modify(!_)
          )
        ), "Dense only"
      )

    def settingsPane(state: BrowserState) = {
      <.div(
        S.settingsContainer,
        <.div(
          S.partitionChooser,
          partitionChoice(state, "Train", DatasetPartition.Train),
          partitionChoice(state, "Dev", DatasetPartition.Dev)
        ),
        <.div(
          S.domainChooser,
          domainChoice(state, "Wikipedia", Domain.Wikipedia),
          domainChoice(state, "Wikinews",  Domain.Wikinews),
          domainChoice(state, "TQA",       Domain.TQA)
        ),
        <.div(
          S.denseOnlyToggle,
          denseOnlyCheckbox(state)
        )
      ), // settings container
    }

    def documentText(props: BrowserProps, doc: Document, setSentence: Sentence => Callback) = {
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
      index: DataIndex,
      filter: FilterState,
      curDocMeta: DocumentMetadata,
      setDocMeta: DocumentMetadata => Callback
    ) = {
      <.div(
        S.documentSelectionPane,
        curDocuments(index, filter).toVdomArray { docMeta =>
          <.div(
            ^.key := docMeta.id.toString,
            if(docMeta == curDocMeta) S.currentSelectionEntry else S.nonCurrentSelectionEntry,
            ^.onClick --> setDocMeta(docMeta),
            <.span(
              S.selectionEntryText,
              docMeta.title
            )
          )
        }
      )
    }

    def sentenceSelectionPane(
      document: Document,
      curSentence: Sentence,
      setSentence: Sentence => Callback
    ) = {
      <.div(
        S.sentenceSelectionPane,
        document.sentences.toVdomArray { sentence =>
          <.div(
            ^.key := sentence.sentenceId,
            if(sentence == curSentence) S.currentSelectionEntry else S.nonCurrentSelectionEntry,
            ^.onClick --> setSentence(sentence),
            <.span(
              S.selectionEntryText,
              Text.render(sentence.sentenceTokens)
            )
          )
        }
      )
    }

    def verbEntryDisplay(
      verb: VerbEntry,
      curSentence: Sentence
    ) = {
      <.div(
        S.verbEntryDisplay,
        ^.key := verb.verbIndex,
        <.div(
          S.verbHeading,
          <.span(
            S.verbHeadingText,
            curSentence.sentenceTokens(verb.verbIndex)
          )
        ),
        <.table(
          S.verbQAsTable,
          <.tbody(
            S.verbQAsTableBody,
            verb.questionLabels.toList.map(_._2).sortBy(_.questionString).toVdomArray { label =>
              qaLabelRow(label)
            }
          )
        )
      )
    }

    def qaLabelRow(label: QuestionLabel) = {
      <.tr(
        S.qaPairRow,
        ^.key := label.questionString,
        <.td(S.questionCell, <.span(S.questionText, label.questionString)),
        <.td(S.answerCell, <.span(S.answerText, "TODO answers"))
      )
    }

    def render(props: BrowserProps, state: BrowserState) = {
      <.div(
        S.mainContainer,
        settingsPane(state),
        IndexFetch.make(request = (), sendRequest = _ => props.qasrl.getDataIndex) {
          case IndexFetch.Loading => <.div("Loading metadata...")
          case IndexFetch.Loaded(index) =>
            DocMetaLocal.make(initialValue = curDocuments(index, state.filter).head) {
              case (curDocMeta, setDocMeta) =>
                <.div(
                  S.dataContainer,
                  docSelectionPane(index, state.filter, curDocMeta, setDocMeta),
                  DocFetch.make(request = curDocMeta.id, sendRequest = id => props.qasrl.getDocument(id)) {
                    case DocFetch.Loading => <.div(
                      S.documentContainer,
                      <.span(S.loadingNotice, "Loading document...")
                    )
                    case DocFetch.Loaded(doc) => SentLocal.make(initialValue = doc.sentences.head) {
                      case (curSentence, setSentence) => <.div(
                        S.documentContainer,
                        sentenceSelectionPane(doc, curSentence, setSentence),
                        <.div(
                          S.sentenceDisplayPane,
                          <.div(
                            <.span(
                              S.sentenceText,
                              Text.render(curSentence.sentenceTokens)
                            )
                          ),
                          curSentence.verbEntries.values.toList.sortBy(_.verbIndex).toVdomArray { verb =>
                            verbEntryDisplay(verb, curSentence)
                          }
                        )
                      )
                    }
                  }
                )
            }
        }
      )
    }
  }

  val Browser = ScalaComponent.builder[BrowserProps]("Browser")
    .initialStateFromProps((props: BrowserProps) => BrowserState.initial(props))
    .renderBackend[BrowserBackend]
    .build
}
