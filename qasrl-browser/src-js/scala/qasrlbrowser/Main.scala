package qasrlbrowser

import org.scalajs.dom

import scalacss.DevDefaults._

import qasrl.bank.DataIndex
import qasrl.data.Dataset

object Main {
  def main(args: Array[String]): Unit = {
    BrowserStyles.addToDocument()

    // println("Loading data meta index...")

    // val dataIndex = {
    //   import scala.scalajs.js
    //   import io.circe.scalajs.decodeJs
    //   import io.circe.generic.auto._
    //   import io.circe.syntax._
    //   import collection.mutable
    //   import cats.Order.catsKernelOrderingForOrder
    //   decodeJs[DataIndex](
    //     js.Dynamic.global.dataMetaIndex.asInstanceOf[js.Any]
    //   ).right.get
    // }

    // println("Data meta index loaded.")

    import qasrl.bank.service.WebClientDocumentService
    val dataService = new WebClientDocumentService("http://nlp.cs.washington.edu:8080")

    Browser.Component(Browser.Props(dataService)).renderIntoDOM(
      dom.document.getElementById("browser")
    )
  }
}
