package qasrlbrowser

import qasrl.bank.service.{CacheCall, Cached, Remote}

import scala.concurrent.ExecutionContext.Implicits.global

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

import scalacss.DevDefaults._
import scalacss.ScalaCssReact._

import monocle._
import monocle.macros._
import japgolly.scalajs.react.MonocleReact._

import scala.concurrent.Future

// for many-time loading of content, e.g., via ajax, assuming repeated calls will be cached
class CacheCallContentComponent[Request, Response] {

  sealed trait State
  case object Loading extends State
  case class Loaded(content: Response) extends State

  case class Props(
    request: Request, // will possibly change
    sendRequest: Request => CacheCall[Response], // expected NOT to change
    willLoad: (Response => Callback) = (_ => Callback.empty), // expected NOT to change
    didLoad: (Response => Callback) = (_ => Callback.empty), // expected NOT to change
    render: (State => VdomElement) // expected NOT to change
  )

  class Backend(scope: BackendScope[Props, State]) {

    def load(props: Props): Callback =
      props.sendRequest(props.request) match {
        case Cached(response) => scope.setState(Loaded(response))
        case Remote(future) => scope.setState(Loading) >> Callback.future {
          future.map { response =>
            props.willLoad(response) >> scope.setState(Loaded(response)) >> props.didLoad(response)
          }
        }
      }
    def render(props: Props, s: State) =
      props.render(s)
  }

  val Component = ScalaComponent
    .builder[Props]("Ajax Loadable")
    .initialState(Loading: State)
    .renderBackend[Backend]
    .componentDidMount(context => context.backend.load(context.props))
    .componentWillReceiveProps(context =>
    if(context.currentProps.request == context.nextProps.request) {
      Callback.empty
    } else {
      context.backend.load(context.nextProps)
    }
  )
    .build

  def make(
    request: Request,
    sendRequest: Request => CacheCall[Response],
    willLoad: (Response => Callback) = (_ => Callback.empty),
    didLoad: (Response => Callback) = (_ => Callback.empty))(
    render: (State => VdomElement)
  ) = {
    Component(Props(request, sendRequest, willLoad, didLoad, render))
  }
}
