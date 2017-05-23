package edu.knowitall.openie

import scala.collection.JavaConverters._
import com.google.common.base.CharMatcher
import edu.knowitall.chunkedextractor.BinaryExtractionInstance
import edu.knowitall.chunkedextractor.Relnoun
import edu.knowitall.chunkedextractor.confidence.RelnounConfidenceFunction
import edu.knowitall.collection.immutable.Interval
import edu.knowitall.openie.util.ReplaceChars
import edu.knowitall.srlie.SrlExtraction
import edu.knowitall.srlie.SrlExtractionInstance
import edu.knowitall.srlie.SrlExtractor
import edu.knowitall.srlie.confidence.SrlConfidenceFunction
import edu.knowitall.tool.chunk.ChunkedToken
import edu.knowitall.tool.chunk.OpenNlpChunker
import edu.knowitall.tool.parse.ClearParser
import edu.knowitall.tool.parse.DependencyParser
import edu.knowitall.tool.postag.ClearPostagger
import edu.knowitall.tool.postag.ClearPostagger
import edu.knowitall.tool.srl.ClearSrl
import edu.knowitall.tool.srl.Srl
import edu.knowitall.tool.stem.MorphaStemmer
import edu.knowitall.tool.tokenize.ClearTokenizer
import edu.iitd.cse.open_nre.onre.helper.MayIHelpYou
import edu.iitd.cse.open_nre.onre.domain.OnreExtraction
import org.apache.commons.lang.mutable.Mutable

class OpenIE(parser: DependencyParser = new ClearParser(), srl: Srl = new ClearSrl(), triples: Boolean = false, includeUnknownArg2: Boolean = false) {
  // confidence functions
  val srlieConf = SrlConfidenceFunction.loadDefaultClassifier()
  val relnounConf = RelnounConfidenceFunction.loadDefaultClassifier()

  // sentence pre-processors
  val tokenizer = new ClearTokenizer()
  val postagger = new ClearPostagger(tokenizer)
  val chunkerOIE = new OpenNlpChunker(postagger)
  
  // subextractors
  val relnoun = new Relnoun(true, true, includeUnknownArg2)
  val srlie = new SrlExtractor(srl)

  /***
   * Remove problematic characters from a line before extracting.
   */
  // Added to overcome clearNLP tokenize issue
  val replaceChars = new ReplaceChars()
  replaceChars.load()

  def clean(line: String): String = {
    var cleaned = line
    cleaned = replaceChars.replacenow(cleaned)

    cleaned = CharMatcher.WHITESPACE.replaceFrom(cleaned, ' ')
    cleaned = CharMatcher.JAVA_ISO_CONTROL.removeFrom(cleaned)

    cleaned
  }

  def apply(sentence: String): Seq[Instance] = extract(sentence)
  def extract(sentence: String): Seq[Instance] = extract(sentence,chunkerOIE,triples)
  def extract(sentence: String, chunker : OpenNlpChunker = chunkerOIE, isTriples: Boolean = triples): Seq[Instance] = {
    // pre-process the sentence
    val cleaned = clean(sentence)
    val chunked = chunker(cleaned) map MorphaStemmer.lemmatizePostaggedToken
    val parsed = parser(cleaned)
    
    // run extractors
    val srlExtrs: Seq[SrlExtractionInstance] =
      if (isTriples) srlie(parsed).flatMap(_.triplize())
      else srlie(parsed)
    val relnounExtrs = relnoun(chunked)
    val onreExtrs = MayIHelpYou.runMe(parsed).asScala.keys;

    def convertSrl(inst: SrlExtractionInstance): Instance = {
      def offsets(part: SrlExtraction.MultiPart) = {
        var intervals = part.intervals
        // hack: sorted should not be necessary
        // see https://github.com/knowitall/srlie/issues/8)
        var tokens = part.tokens.sorted
        var offsets = List.empty[Interval]
        while (!intervals.isEmpty) {
          val sectionTokens = tokens.take(intervals.head.size)
          tokens = tokens.drop(intervals.head.size)
          intervals = intervals.drop(1)

          offsets ::= Interval.open(sectionTokens.head.offsets.start, sectionTokens.last.offsets.end)
        }

        offsets.reverse
      }
      try {
        val extr = new Extraction(
          rel = new Relation(inst.extr.rel.text, offsets(inst.extr.rel)),
          // can't use offsets field due to a bug in 1.0.0-RC2
          arg1 = new SimpleArgument(inst.extr.arg1.text, Seq(Interval.open(inst.extr.arg1.tokens.head.offsets.start, inst.extr.arg1.tokens.last.offsets.end))),
          arg2s = inst.extr.arg2s.map { arg2 =>
            val offsets = Seq(Interval.open(arg2.tokens.head.offsets.start, arg2.tokens.last.offsets.end))
            arg2 match {
              case arg2: SrlExtraction.TemporalArgument => new TemporalArgument(arg2.text, offsets)
              case arg2: SrlExtraction.LocationArgument => new SpatialArgument(arg2.text, offsets)
              case arg2: SrlExtraction.Argument => new SimpleArgument(arg2.text, offsets)
            }
          },
          context = inst.extr.context.map(context => new Context(context.text, Seq(Interval.open(context.tokens.head.offsets.start, context.tokens.last.offsets.end)))),
          negated = inst.extr.negated,
          passive = inst.extr.passive)
        Instance(srlieConf(inst), sentence, extr)
      }
      catch {
        case e: Exception =>
          throw new OpenIEException("Error converting SRL instance to Open IE instance: " + inst, e)
      }
    }

    def convertRelnoun(inst: BinaryExtractionInstance[ChunkedToken]): Instance = {
      val extr = new Extraction(
        rel = new Relation(inst.extr.rel.text, Seq(inst.extr.rel.offsetInterval)),
        arg1 = new SimpleArgument(inst.extr.arg1.text, Seq(inst.extr.arg1.offsetInterval)),
        arg2s = Seq(new SimpleArgument(inst.extr.arg2.text, Seq(inst.extr.arg2.offsetInterval))),
        context = None,
        negated = false,
        passive = false)
      Instance(relnounConf(inst), sentence, extr)
    }
    
    def convertOnre(inst: OnreExtraction): Instance = {
      def arg2s (inst: OnreExtraction) = {
        var arg2s:Seq[edu.knowitall.openie.Argument] = Seq(new SimpleArgument(inst.quantity.text
            , Seq(null)))
        
        arg2s
      }
      val totalNumberOfPatterns = 1000
      val confidence = (totalNumberOfPatterns-inst.patternNumber)/(totalNumberOfPatterns.toFloat)
      println(confidence)
      val extr = new Extraction(
        rel = new Relation(inst.relation.text, Seq(inst.relation.offsetInterval)),
        arg1 = new SimpleArgument(inst.argument.text, Seq(inst.argument.offsetInterval)),
        arg2s = arg2s (inst),
        context = None,
        negated = false,
        passive = false)
      Instance(confidence, sentence, extr)
    }
    
    def removeDuplicateExtractions(instances : Seq[Instance]): Seq[Instance] = {
      var relationsMap = collection.mutable.Map[String, Boolean]()
      var newInstances = List[Instance]()
      
      for ( instance <- instances) {
        if(!relationsMap.contains(instance.extraction.rel.text)) {
          newInstances ::= instance
          relationsMap.put(instance.extraction.rel.text, true) 
        }
      }
      newInstances
    }

    val extrs = (srlExtrs map convertSrl) ++ (relnounExtrs map convertRelnoun) ++ (onreExtrs map convertOnre) 

    removeDuplicateExtractions(extrs)
  } 
}


