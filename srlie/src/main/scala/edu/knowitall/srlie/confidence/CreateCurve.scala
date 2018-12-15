package edu.knowitall.srlie.confidence

import java.io.File
import scala.io.Source
import edu.knowitall.common.Resource
import edu.knowitall.common.Analysis
import java.io.PrintWriter

object CreateCurve extends App {
  run(new File(args(0)), new File(args(1)))

  def run(inputFile: File, outputFile: File) {
    val annotations = Resource.using(Source.fromFile(inputFile, "UTF8")) { source =>
      (for {
        line <- source.getLines
        Array(annotation, confidence, _ @ _*) = line.split("\t")
        if !annotation.isEmpty
      } yield {
        (confidence.toDouble, annotation == "1")
      }).toList
    }

    val points = Analysis.precisionYieldMeta(annotations.sortBy(-_._1))
    val auc = Analysis.areaUnderCurve(points.map { case (conf, p, y) => (p, y) })

    Resource.using(new PrintWriter(outputFile, "UTF8")) { writer =>
      writer.println("AUC: " + auc)
      writer.println("Points:")
      points.map(_.productIterator.mkString("\t")) foreach writer.println
    }
  }
}