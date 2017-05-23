package edu.knowitall.openie

import edu.knowitall.collection.immutable.Interval

/***
 * The abstract representation of an extraction.
 *
 * @param  arg1  the Argument 1
 * @param  rel  the Relation
 * @param  arg2s  a sequence of the Argument 2s
 * @param  context  an optional representation of the context for this extraction
 * @param  negated  whether this is a true or false assertion
 * @param  passive  whether this is a passive or active assertion
 */
case class Extraction(arg1: Argument, rel: Relation, arg2s: Seq[Argument], context: Option[Part], negated: Boolean, passive: Boolean) {
  def tripleString = s"(${arg1.displayText}; ${rel.displayText}; ${arg2s.map(_.displayText).mkString("; ")})"
  override def toString = {
    val basic = tripleString
    context match {
      case Some(context) => context + ":" + basic
      case None => basic
    }
  }
}

/***
 * A component of an extraction.
 */
abstract class Part {
  def text: String
  def offsets: Seq[Interval]

  def displayText = text
}

case class Context(text: String, offsets: Seq[Interval]) extends Argument

abstract class Argument extends Part
abstract class SemanticArgument extends Part
case class SimpleArgument(text: String, offsets: Seq[Interval]) extends Argument
case class SpatialArgument(text: String, offsets: Seq[Interval]) extends Argument {
  override def displayText = "L:" + text
}
case class TemporalArgument(text: String, offsets: Seq[Interval]) extends Argument {
  override def displayText = "T:" + text
}

case class Relation(text: String, offsets: Seq[Interval]) extends Part
