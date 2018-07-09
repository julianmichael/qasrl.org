package qasrl

import cats.Order
import qasrl.data.Sentence

package object bank {
  implicit val qasrlDataSentenceOrder = Order.by[Sentence, SentenceId](s => SentenceId.fromString(s.sentenceId))

  // TODO consider contributing to cats
  // implicit class RichOrderObject(val o: Order.type) extends AnyVal {
  //   def whenEqual[A](orders: Order[A]*): Order[A] =
  //     new Order[A] {
  //       def compare(x: A, y: A) = {
  //         orders.reverse.foldRight(0) { case (newOrder, prevValue) =>
  //           if(prevValue != 0) prevValue else newOrder.compare(x, y)
  //         }
  //       }
  //     }
  // }
}
