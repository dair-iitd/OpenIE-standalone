package edu.knowitall.openie

import edu.knowitall.tool.chunk.OpenNlpChunker
import edu.knowitall.tool.postag.OpenNlpPostagger
import edu.knowitall.tool.tokenize.OpenNlpTokenizer



import org.scalatest._

class OpenIESpecTest extends FlatSpec with Matchers {
  "OpenIE" should "instantiate and extract correctly" in {
    val openie = new OpenIE()

    val insts = openie("U.S. president Obama gave a speech")

    insts.size should be (2)
    insts.map(_.extraction.toString).sorted should be (Seq("(Obama; [is] president [of]; United States)", "(U.S. president Obama; gave; a speech)"))

    // sentence pre-processors
    val tokenizertest = new OpenNlpTokenizer()
    val postaggertest = new OpenNlpPostagger(tokenizertest)
    val chunkertest = new OpenNlpChunker(postaggertest)

    val inststest = openie.extract("U.S. president Obama gave a speech",chunkertest)
    inststest.size should be (2)
    inststest.map(_.extraction.toString).sorted should be (Seq("(Obama; [is] president [of]; United States)", "(U.S. president Obama; gave; a speech)"))

  }
}
