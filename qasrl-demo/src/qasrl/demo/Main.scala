package qasrl.demo

import qasrlbrowser._

import org.scalajs.dom

import scalacss.DevDefaults._

object Main {
  def main(args: Array[String]): Unit = {
    DemoStyles.addToDocument()
    import qasrl.bank.service.WebClientDocumentService
    val apiEndpoint = "http://nlp.cs.washington.edu:5050/parse"

    Demo.Component(Demo.Props(apiEndpoint)).renderIntoDOM(
      dom.document.getElementById("demo")
    )
  }
}
