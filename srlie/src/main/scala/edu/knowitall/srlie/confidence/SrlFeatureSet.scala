package edu.knowitall.srlie.confidence

import edu.knowitall.tool.conf.FeatureSet
import edu.knowitall.tool.conf.Feature
import edu.knowitall.srlie.SrlExtractionInstance
import scala.collection.immutable.SortedMap
import edu.knowitall.tool.srl.FrameHierarchy
import edu.knowitall.srlie.SrlExtraction.Part
import java.util.regex.Pattern
import java.util.regex.Pattern

object SrlFeatureSet extends FeatureSet[SrlExtractionInstance, Double](SrlFeatures.featureMap)

/** Features defined for OllieExtractionInstances */
object SrlFeatures {
  type SrlFeature = Feature[SrlExtractionInstance, Double]

  implicit def boolToDouble(bool: Boolean) = if (bool) 1.0 else 0.0

  /** Turn a hierarchical frame to a list of frames. */
  def flattenFrame(frame: FrameHierarchy): Seq[FrameHierarchy] = {
    if (frame.children.isEmpty) Seq.empty
    else frame +: frame.children.flatMap(flattenFrame(_))
  }

  object hierarchicalFrames extends SrlFeature("hierarchical frames") {
    override def apply(inst: SrlExtractionInstance): Double = {
      !inst.frame.children.isEmpty
    }
  }

  object fewFrameArguments extends SrlFeature("< 2 frame arguments") {
    override def apply(inst: SrlExtractionInstance): Double = {
      flattenFrame(inst.frame).exists(_.frame.arguments.size < 2)
    }
  }

  object manyFrameArguments extends SrlFeature("> 2 frame arguments") {
    override def apply(inst: SrlExtractionInstance): Double = {
      flattenFrame(inst.frame).exists(_.frame.arguments.size > 2)
    }
  }

  object intransitiveExtraction extends SrlFeature("intransitive extraction") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg2s.size == 0
    }
  }

  object context extends SrlFeature("extraction has context") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.context.isDefined
    }
  }

  object activeExtraction extends SrlFeature("active extractor") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.active
    }
  }

  object naryExtraction extends SrlFeature("nary extraction") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg2s.size > 1
    }
  }

  object pronoun {
    def extraPronouns = Set("ive", "im", "things", "thing")
  }
  object arg1ContainsPronoun extends SrlFeature("arg1 contains a pronoun or EX") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg1.tokens.exists(tok => tok.isPronoun ||
        tok.postag == "EX" ||
        pronoun.extraPronouns.contains(tok.string.toLowerCase))
    }
  }

  object arg2ContainsPronoun extends SrlFeature("at least one arg2 contains a pronoun or EX") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg2s.exists(_.tokens.exists(tok => tok.isPronoun || tok.postag == "EX" || tok.string.toLowerCase == "ive"))
    }
  }

  object weirdArg {
    val blacklist = Set("that", "those", "ive", "im")
  }
  object weirdArg1 extends SrlFeature("arg1 is blacklisted") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg1.tokens.forall(tok =>
        weirdArg.blacklist contains tok.string)
    }
  }

  object weirdArg2 extends SrlFeature("arg2 is blacklisted") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg2s.exists(_.tokens.exists(tok =>
        weirdArg.blacklist contains tok.string))
    }
  }

  object arg1Noun extends SrlFeature("arg1 is noun") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg1.tokens.exists(tok => tok.isNoun || tok.isPronoun)
    }
  }

  object arg2Noun extends SrlFeature("at least one arg2 is noun") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg2s.exists(_.tokens.exists(tok =>
        tok.isNoun || tok.isPronoun))
    }
  }

  object arg1Proper extends SrlFeature("arg1 is proper") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg1.tokens.exists(_.isProperNoun)
    }
  }

  object arg2Proper extends SrlFeature("at least one arg2 is proper") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg2s.exists(_.tokens.exists(_.isProperNoun))
    }
  }

  object arg1BeforeRel extends SrlFeature("arg1 appears before rel in sentence") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg1.interval leftOf inst.extr.relation.span
    }
  }

  object arg2sAfterRel extends SrlFeature("all arg2s appear after rel in sentence") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg2s.forall(_.interval rightOf inst.extr.relation.span)
    }
  }

  object arg1BordersRel extends SrlFeature("arg1 borders rel") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg1.interval borders inst.extr.relation.span
    }
  }

  object arg2BordersRel extends SrlFeature("arg2 borders rel") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg2s.exists(_.interval borders inst.extr.relation.span)
    }
  }

  object relContainsVerb extends SrlFeature("rel contains verb") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.relation.tokens exists (_.isVerb)
    }
  }

  object relContiguous extends SrlFeature("rel contiguous") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.relation.span.forall { i =>
        inst.extr.relation.tokens.exists(tok => tok.tokenInterval contains i)
      }
    }
  }

  object longRelation extends SrlFeature("rel contains > 10 tokens") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.relation.tokens.length >= 10
    }
  }

  object longArg1 extends SrlFeature("arg1 contains > 10 tokens") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg1.tokens.length
    }
  }

  object longArg2 extends SrlFeature("an arg2 contains > 10 tokens") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.extr.arg2s.exists(_.tokens.length >= 10)
    }
  }

  object shortSentence extends SrlFeature("sentence contains < 10 tokens") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.dgraph.nodes.size > 20
    }
  }

  object longSentence extends SrlFeature("sentence contains > 20 tokens") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.dgraph.nodes.size > 20
    }
  }

  object longerSentence extends SrlFeature("sentence contains > 40 tokens") {
    override def apply(inst: SrlExtractionInstance): Double = {
      inst.dgraph.nodes.size > 40
    }
  }

  class BadCharacters(getPart: SrlExtractionInstance => Part, partName: String) extends SrlFeature(partName + " contains bad characters") {
    val notCapsPattern = Pattern.compile("[^A-Z]")
    val weirdChars = Pattern.compile("[^AEIOUYaeiouy0-9]")
    override def apply(inst: SrlExtractionInstance): Double = {
      // are there more than 5 caps?
      if (notCapsPattern.matcher(getPart(inst).text).replaceAll("").length() > 5)
        1.0
      // are there not enough good characters?
      else if (weirdChars.matcher(getPart(inst).text).replaceAll("").length() < 2)
        1.0
      else
        0.0
    }
  }

  def features: Seq[SrlFeature] = Seq(
    // context,
    // activeExtraction,
    hierarchicalFrames,
    fewFrameArguments,
    manyFrameArguments,
    intransitiveExtraction,
    naryExtraction,
    arg1ContainsPronoun,
    arg2ContainsPronoun,
    arg1Proper,
    arg2Proper,
    arg1Noun,
    arg2Noun,
    weirdArg1,
    weirdArg2,
    arg1BeforeRel,
    arg2sAfterRel,
    arg1BordersRel,
    arg2BordersRel,
    relContainsVerb,
    relContiguous,
    longRelation,
    longArg1,
    longArg2,
    // shortSentence,
    // longSentence,
    // longerSentence,
    new BadCharacters(inst => inst.extr.arg1, "arg1"),
    new BadCharacters(inst => inst.extr.relation, "rel"))

  def featureMap: SortedMap[String, SrlFeature] = {
    (for (f <- features) yield (f.name -> Feature.from(f.name, f.apply _)))(scala.collection.breakOut)
  }
}
