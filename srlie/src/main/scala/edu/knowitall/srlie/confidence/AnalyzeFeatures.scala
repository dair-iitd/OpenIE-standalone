package edu.knowitall.srlie.confidence

import java.io.File
import scala.io.Source
import scala.util.control.Exception
import edu.knowitall.tool.parse.ClearParser
import edu.knowitall.tool.srl.ClearSrl
import edu.knowitall.tool.srl.Srl
import edu.knowitall.tool.parse.graph.DependencyGraph
import edu.knowitall.tool.srl.FrameHierarchy
import edu.knowitall.tool.srl.Frame
import edu.knowitall.srlie.SrlExtractor
import edu.knowitall.srlie.SrlExtraction
import edu.knowitall.srlie.SrlExtractionInstance
import edu.knowitall.common.Resource

object AnalyzeFeatures extends App {
  run()

  def run() {
    lazy val parser = new ClearParser()
    val srl = new ClearSrl()
    val conf = SrlConfidenceFunction.loadDefaultClassifier()
    val srlExtractor = new SrlExtractor()

    def graphify(line: String) = {
      (Exception.catching(classOf[DependencyGraph.SerializationException]) opt DependencyGraph.deserialize(line)) match {
        case Some(graph) => graph
        case None => parser.dependencyGraph(line)
      }
    }

    for (line <- Source.stdin.getLines) {
      val graph = graphify(line)
      val extrs = srlExtractor(graph)

      def printExtraction(inst: SrlExtractionInstance) = {
        println(inst.extr)
        println(conf(inst))
        SrlFeatures.featureMap.foreach { case(name, feature) =>
          val value = feature(inst)
          println(Iterable(name, value, ("%.2f" format (value.toDouble * conf.featureWeights(name)))).mkString("\t"))
        }
        println()
      }

      println("extractions:")
      extrs foreach printExtraction

      println()

      println("triples:")
      extrs.flatMap(_.triplize(true)) foreach printExtraction

    }
  }
}
