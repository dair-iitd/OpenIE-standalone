package edu.knowitall.openie.util

import edu.knowitall.tool.segment.Segmenter

class SentenceIterator(sentencer: Segmenter, private var lines: BufferedIterator[String]) extends Iterator[String] {
  var sentences: Iterator[String] = Iterator.empty

  lines.dropWhile(_.trim.isEmpty)

  def nextSentences = {
    val (paragraph, rest) = lines.span(!_.trim.isEmpty)
    lines = rest.dropWhile(_.trim.isEmpty).buffered
    sentencer.segmentTexts(paragraph.mkString(" ")).iterator.buffered
  }

  def hasNext: Boolean = {
    if (sentences.hasNext) {
      true
    }
    else if (!lines.hasNext) {
      false
    }
    else {
      sentences = nextSentences
      sentences.hasNext
    }
  }
  
  def next: String = {
    if (sentences.hasNext) {
      sentences.next()
    }
    else {
      sentences = nextSentences
	  sentences.next()
    }
  }
}
