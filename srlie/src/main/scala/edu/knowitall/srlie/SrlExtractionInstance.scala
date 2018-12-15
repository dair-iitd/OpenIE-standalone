package edu.knowitall.srlie

import edu.knowitall.tool.srl.FrameHierarchy
import edu.knowitall.tool.parse.graph.DependencyGraph

case class SrlExtractionInstance(extr: SrlExtraction, frame: FrameHierarchy, dgraph: DependencyGraph) {
  def extraction = extr

  override def toString = extr.toString + " <- " + frame.toString

  def triplize(includeDobj: Boolean = true): Seq[SrlExtractionInstance] = {
    extr.triplize(includeDobj).map(extr => this.copy(extr = extr))
  }
}
