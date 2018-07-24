package qasrlbrowser

import org.scalajs.dom

import scalacss.DevDefaults._

import qasrl.bank.DataIndex
import qasrl.data.Dataset

object Main {
  def main(args: Array[String]): Unit = {
    BrowserStyles.addToDocument()

    val dataIndex = {
      import scala.scalajs.js
      import io.circe.scalajs.decodeJs
      import io.circe.generic.auto._
      import io.circe.syntax._
      import collection.mutable
      import cats.Order.catsKernelOrderingForOrder
      decodeJs[DataIndex](
        js.Dynamic.global.dataMetaIndex.asInstanceOf[js.Any]
      ) match {
        case Right(index) => index
        case Left(err) =>
          System.err.println(err)
          null: DataIndex
      }
    }

    import qasrl.bank.service.WebClientDocumentService
    val dataService = new WebClientDocumentService(
      "http://localhost:8080"
    )

    // def makeCallCache = {
    //   import scala.collection.mutable
    //   import DocumentService._
    //   val indexCache = mutable.Map.empty[GetDataIndex.type, Map[GetDataIndex.type, DataIndex]]
    //   indexCache.put(GetDataIndex, dataIndex)
    //   val documentCache = 

    //   new (RequestA ~> λ[A => mutable.Map[RequestA, A]]) {
    //     def apply[A](reqA: RequestA[A]) = reqA match {
    //       case object GetDataIndex =>
    //       case class GetDocument(id: DocumentId) extends RequestA[Document]
    //       case class SearchDocuments(query: Set[LowerCaseString]) extends RequestA[Set[DocumentId]]
    //     }
    //   }
    // }

    // def cachifyCalls[F[_]](
    //   f: (F ~> Future),
    //   getCache: F ~> λ[A => mutable.Map[RequestA, A]]
    // ): (F ~> CacheCall) = {

    // }

    Browser.Component(Browser.Props(dataService)).renderIntoDOM(
      dom.document.getElementById("browser")
    )
  }
}
