package edu.knowitall.openie

import edu.knowitall.tool.chunk.OpenNlpChunker
import edu.knowitall.tool.postag.OpenNlpPostagger
import edu.knowitall.tool.tokenize.OpenNlpTokenizer
import edu.knowitall.tool.parse.DependencyParser
import edu.knowitall.tool.srl.Srl
import edu.knowitall.tool.parse.ClearParser
import edu.knowitall.tool.srl.ClearSrl


/***
 * A wrapper of OpenIE.
 * We recommend, extractTs should be used for short lived threads. 
 * Otherwise create opennlp componant for each thread, then pass them to extract or OpenIE.extract
 */

class OpenIETs(parser: DependencyParser = new ClearParser(), srl: Srl = new ClearSrl(), triples: Boolean = false) {
  
  val openie = new OpenIE(parser,srl,triples)

  val tokenizerNonThreadSafe = new OpenNlpTokenizer()
  val postaggerNonThreadSafe = new OpenNlpPostagger(tokenizerNonThreadSafe)
  val chunkerNonThreadSafe   = new OpenNlpChunker(postaggerNonThreadSafe)

  def extractTs(sentence: String): Seq[Instance] = extractTs(sentence, triples)
  
  def extractTs(sentence: String, isTriples: Boolean = triples): Seq[Instance] = {
  //sentence pre-processors, class vars are not thread safe but models are
    val tokenizer = new OpenNlpTokenizer(tokenizerNonThreadSafe.model)
    val postagger = new OpenNlpPostagger(postaggerNonThreadSafe.model, tokenizer)
    val chunker = new OpenNlpChunker(chunkerNonThreadSafe.model, postagger)

    openie.extract(sentence, chunker, isTriples)
  }
  
  def extract(sentence: String, chunker : OpenNlpChunker = chunkerNonThreadSafe, isTriples: Boolean = triples): Seq[Instance] = { 
    openie.extract(sentence, chunker, isTriples)
  }
}
