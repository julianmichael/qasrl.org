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
    shouldRefresh: A => Boolean = _ => true,
    render: StateVal[A] => VdomElement
  )

  class Backend(scope: BackendScope[Props, State]) {
    def set(a: A): Callback = scope.setState(a)
    def render(props: Props, state: State) = props.render(StateVal(state, set _))
  }

  val Component = ScalaComponent
    .builder[Props]("Local")
    .initialStateFromProps(_.initialValue)
    .renderBackend[Backend]
    .componentWillReceiveProps(context =>
    if(context.currentProps.initialValue != context.nextProps.initialValue &&
         context.nextProps.shouldRefresh(context.state)
    ) {
      context.backend.set(context.nextProps.initialValue)
    } else Callback.empty
  )
    .build

  def make(
    initialValue: A,
    shouldRefresh: A => Boolean = _ => true)(
    render: StateVal[A] => VdomElement
  ) = {
    Component(Props(initialValue, shouldRefresh, render))
  }

  // def makeEmptyInit(
  //   shouldRefresh: A => Boolean = _ => true
  // )(
  //   render: StateVal[A] => VdomElement)(
  //   implicit M: Monoid[A]
  // ) = {
  //   Component(Props(M.empty, shouldRefresh, render))
  // }
}
