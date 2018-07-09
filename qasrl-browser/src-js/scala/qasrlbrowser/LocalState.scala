package qasrlbrowser

import scalajs.js
import org.scalajs.dom
import org.scalajs.dom.raw._

import scala.concurrent.ExecutionContext.Implicits.global

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

import scalacss.DevDefaults._
import scalacss.ScalaCssReact._

import monocle._
import monocle.macros._
import monocle.std.option
import japgolly.scalajs.react.MonocleReact._

import cats.Monoid
import cats.data.NonEmptyList

class LocalStateComponent[A] {
  type State = A
  type Context = A => Callback
  case class Props(
    initialValue: A,
    render: (State, Context) => VdomElement
  )

  object Props {

    def apply(render: (State, Context) => VdomElement)(
      implicit m: Monoid[A]
    ): Props =
      Props(m.empty, render)
  }

  class Backend(scope: BackendScope[Props, State]) {
    def set(a: A): Callback = scope.setState(a)
    val context = set _
    def render(props: Props, state: State) = props.render(state, context)
  }

  val Component = ScalaComponent
    .builder[Props]("Local")
    .initialStateFromProps(_.initialValue)
    .renderBackend[Backend]
    .componentWillReceiveProps(context =>
    if(context.currentProps.initialValue == context.nextProps.initialValue) {
      Callback.empty
    } else {
      context.backend.set(context.nextProps.initialValue)
    }
  )
    .build

  def make(
    initialValue: A)(
    render: (State, Context) => VdomElement
  ) = {
    Component(Props(initialValue, render))
  }

  def makeEmptyInit(
    render: (State, Context) => VdomElement)(
    implicit M: Monoid[A]
  ) = {
    Component(Props(M.empty, render))
  }
}
