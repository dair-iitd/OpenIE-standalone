package edu.knowitall.srlie.confidence

import java.io.File
import scala.io.Source
import scopt.mutable.OptionParser
import edu.knowitall.tool.conf.BreezeLogisticRegressionTrainer
import edu.knowitall.common.Resource
import edu.knowitall.srlie.SrlExtractor
import edu.knowitall.tool.parse.ClearParser
import edu.knowitall.tool.conf.Labelled
import edu.knowitall.srlie.SrlExtractionInstance
import scala.util.Random
import edu.knowitall.tool.parse.graph.DependencyGraph
import edu.knowitall.common.Analysis
import org.slf4j.LoggerFactory

object TrainSrlConfidence {
  val logger = LoggerFactory.getLogger(this.getClass)
  def main(args: Array[String]) {
    object settings extends Settings {
      var inputFile: File = _
      var outputFile: Option[File] = None
      var evaluate: Boolean = false
      var count: Int = Int.MaxValue
    }

    val parser = new OptionParser("scoreextr") {
      arg("gold", "gold set", { path: String => settings.inputFile = new File(path) })
      argOpt("output", "output file", { path: String => settings.outputFile = Some(new File(path)) })
      opt("e", "evaluate", "evaluate using folds", { settings.evaluate = true})
      intOpt("c", "count", "number of sentences to use", { (i: Int) => settings.count = i })
    }

    if (parser.parse(args)) {
      run(settings)
    }
  }

   abstract class Settings {
     def inputFile: File
     def outputFile: Option[File]
     def evaluate: Boolean
     def count: Int
   }

  def run(settings: Settings) = {
    lazy val parser = new ClearParser()
    lazy val extractor = new SrlExtractor()

    logger.info("Creating trainer...")
    val trainer = new BreezeLogisticRegressionTrainer(SrlFeatureSet)
    def train(gold: Map[String, Boolean], instances: Seq[SrlExtractionInstance]) = {
      val data = instances.flatMap { inst =>
        gold.get(inst.extr.basicTripleString).map { label =>
          new Labelled(label, inst)
        }
      }

      trainer.train(data)
    }

    logger.info("Reading input...")
    val input =
      Resource.using (Source.fromFile(settings.inputFile, "UTF8")) { source =>
        source.getLines.map { line =>
          val (score, extraction, sentence) =
            line.split("\t") match {
              case Array(score, extraction, _, _, _, sentence) => (score, extraction, sentence)
              case _ => throw new MatchError("Could not deserialize line: " + line)
            }
          val annotation = score match {
            case "" => None
            case "0" | "1" => Some(if (score == "1") true else false)
          }

          (annotation, extraction, sentence)
        }.toList
      }

    logger.info("Creating gold map...")
    val gold = input.flatMap { case (annotation, extraction, sentence) =>
      annotation.map(extraction -> _)
    }.toMap

    val sentences = Random.shuffle(input.map(_._3).toSet.toSeq).take(settings.count)
    logger.info("Sentence count: " + sentences.size)
    if (settings.evaluate) {
      logger.info("Extracting sentences.")
      case class Sentence(text: String, insts: Seq[SrlExtractionInstance])
      val extracted = sentences.map { sentence =>
        val insts = extractor(parser(sentence))
        Sentence(sentence, insts)
      }

      // use cross validation to test the annotations and feature set
      logger.info("Shuffling sentences.")

      val folds = 10
      val foldWidth = sentences.size / folds
      logger.info("Executing " + folds + " folds of size: " + foldWidth)
      val annotated = for {
        i <- 0 until folds
        _ = logger.info("Executing fold: " + i)
        test = extracted.drop(i * foldWidth).take(foldWidth)
        training = extracted.take(i * foldWidth) ++ extracted.drop((i + 1) * foldWidth)

        // make sure test and train are disjoint
        _ = require((test.map(_.text).toSet intersect training.map(_.text).toSet) == Set.empty,
            "test is not disjoint from training: " + (test.map(_.text).toSet intersect training.map(_.text).toSet))

        classifier = train(gold, training.flatMap(_.insts))

        sentence <- test
        example <-sentence.insts
        annotation <- gold.get(example.extraction.basicTripleString)
      } yield {
        (classifier(example), annotation, example)
      }

      val sorted = annotated.sortBy(-_._1)
      val points = Analysis.precisionYieldMeta(sorted.map { case (conf, annotation, example) => (conf, annotation) })
      val auc = Analysis.areaUnderCurve(points.map { case (conf, y, p) => (y, p) })

      println("AUC: " + auc)
      for (i <- 1 to 10) {
        val threshold = 1.0 - i * 0.1
        println("Y at " + threshold + ": " + points.takeWhile(_._3 > threshold).lastOption.map(_._2).getOrElse("N/A"))
      }
      points foreach { case (conf, y, p) =>
        println(Iterable(conf, y, p).mkString("\t"))
      }

      println("Misclassified:")
      sorted.filter(_._2 == false) foreach { case (conf, annotation, ex) =>
        println(("%2f" format conf) + "\t" + ex.extr + "\t" + ex.dgraph.text)
      }

      /* Charting code does not work with 2.9.3!
      import scalax.chart._
      import scalax.chart.Charting._
      val pys = points.map { case (conf, y, p) => (y, p) }
      val dataset = pys.toXYSeriesCollection()
      val chart = XYLineChart(dataset, title = "Precision - Yield", domainAxisLabel = "Yield", rangeAxisLabel = "Precision")

      // save as file and read bytes
      settings.outputFile.foreach { file => chart.saveAsPNG(file, (1024, 768)) }
      */

    } else {
      // train a classifier
      val insts = sentences map parser.apply flatMap extractor.apply

      val classifier = train(gold, insts)
      settings.outputFile match {
        case Some(file) => classifier.saveFile(file)
        case None =>
          classifier.save(System.out)
      }
    }
  }
}
