package qasrl.bank

import qasrl.data.Sentence
import nlpdata.util.Text
import nlpdata.util.LowerCaseStrings._

package object service {
  def getQueryMatchesInSentence(
    sentence: Sentence,
    query: Set[LowerCaseString]
  ): Set[Int] = {
    sentence.sentenceTokens.indices.filter { i =>
      val token = sentence.sentenceTokens(i)
      query.contains(token.lowerCase) || query.contains(Text.normalizeToken(token).lowerCase) ||
        sentence.verbEntries.get(i).fold(false) { verb =>
          verb.verbInflectedForms.allForms.toSet.intersect(query).nonEmpty
        }
    }.toSet
  }
}
