package edu.knowitall.srlie.nested

import edu.knowitall.srlie.SrlExtraction.Relation
import edu.knowitall.srlie.SrlExtraction.Argument
import SrlNestedExtraction._
import edu.knowitall.srlie.SrlExtraction
import edu.knowitall.tool.parse.graph.DependencyNode
import edu.knowitall.collection.immutable.Interval

case class SrlNestedExtraction(
  extr: SrlExtraction,
  arg1: SrlNestedArgument,
  arg2s: Seq[SrlNestedArgument]) {

  override def toString = "(" + arg1 + ", " + extr.rel + ", " + arg2s.mkString("; ") + ")"

  def rel = extr.rel

  def tokens = {
    nestedArgumentTokens(arg1) ++ rel.tokens ++ arg2s.flatMap(nestedArgumentTokens)
  }
}

object SrlNestedExtraction {
  type SrlNestedArgument = Either[Argument, SrlNestedExtraction]
  def nestedArgumentTokens: Either[Argument, SrlNestedExtraction] => Seq[DependencyNode] = {
    case Left(arg) => arg.tokens
    case Right(extr) => extr.tokens
  }

  def from(extrs: Seq[SrlExtraction]): Seq[SrlNestedExtraction] = {
    val nested = extrs.map { extr =>
      new SrlNestedExtraction(extr, Left(extr.arg1), extr.arg2s.map(Left(_)))
    }

    combine(nested)
  }

  /** Combine extractions e1 and e2 where e2's text is e1's arg2 or arg2.
    *
    * for all extractions e1,
    *   look at all the other extractions e2,
    *     if e1.arg1 == e2.text combine them and repeat
    *     if e1.arg2 == e2.text combine them and repeat
    */
  private def combine(extrs: Seq[SrlNestedExtraction]): Seq[SrlNestedExtraction] = {
    import scalaz.Scalaz._
    import scalaz.Zipper._

    for {
      startZipper <- extrs.toStream.toZipper
      zipper <- startZipper.positions.toStream
    } {
      val extr1 = zipper.focus
      for (extr2 <- zipper.lefts ++ zipper.rights) {
        val extr2Tokens = extr2.tokens.toSet
        if (nestedArgumentTokens(extr1.arg1).toSet == extr2Tokens) {
          return combine(((zipper.lefts.toSeq filterNot (_ == extr2)) :+ extr1.copy(arg1 = Right(extr2))) ++ (zipper.rights filterNot (_ == extr2)))
        }

        for {
          startArg2Zipper <- extr1.arg2s.toStream.toZipper
          arg2Zipper <- startArg2Zipper.positions.toStream
        } {
          val arg2 = arg2Zipper.focus
          if (nestedArgumentTokens(arg2).toSet == extr2Tokens) {
            return combine(((zipper.lefts.toStream filterNot (_ == extr2)) :+ extr1.copy(arg2s = (arg2Zipper.lefts.toSeq :+ Right(extr2)) ++ arg2Zipper.rights)) ++ (zipper.rights filterNot (_ == extr2)))
          }
        }
      }
    }

    return extrs
  }
}
