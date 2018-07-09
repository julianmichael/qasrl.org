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
    // println("Loading dataset...")

    // val dataset = {
    //   import scala.scalajs.js
    //   import io.circe.scalajs.decodeJs
    //   import qasrl.data.JsonCodecs._
    //   import io.circe.generic.auto._
    //   import collection.mutable
    //   decodeJs[Dataset](
    //     js.Dynamic.global.dataset.asInstanceOf[js.Any]
    //   ) match {
    //     case Left(err) => println(err); ???
    //     case Right(dataset) => dataset
    //   }
    // }

    // println("Dataset loaded.")

    // import scala.concurrent._

    // val dataService = new InterpretedDataService(
    //   BasicDataServiceInterpreter(dataset)
    //   //   .compose(new (Id ~> Future) {
    //   //              def apply[A](fa: Id[A]): Future[A] = Future.Success(a)
    //   //            }
    //   // )
    // )

    import qasrl.bank.service.WebClientDocumentService
    val dataService = new WebClientDocumentService("http://localhost:8080")

    import BrowserComponent._
    Browser(BrowserProps(dataService)).renderIntoDOM(
      dom.document.getElementById("browser")
    )
  }
}
