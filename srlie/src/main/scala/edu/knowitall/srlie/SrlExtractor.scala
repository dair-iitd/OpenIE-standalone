package edu.knowitall.srlie

import scala.io.Source
import edu.knowitall.tool.parse.ClearParser
import edu.knowitall.tool.srl.ClearSrl
import edu.knowitall.tool.srl.Srl
import edu.knowitall.tool.parse.graph.DependencyGraph
import scala.util.control.Exception
import java.io.File
import edu.knowitall.common.Resource
import edu.knowitall.tool.srl.FrameHierarchy
import edu.knowitall.tool.srl.Frame
import edu.knowitall.tool.srl.Roles
import edu.knowitall.srlie.confidence.SrlConfidenceFunction
import java.io.PrintWriter
import java.net.URL
import edu.knowitall.srlie.confidence.SrlFeatureSet
import edu.knowitall.common.Timing

class SrlExtractor(val srl: Srl = new ClearSrl()) {
  def apply(dgraph: DependencyGraph): Seq[SrlExtractionInstance] = {
    val frames = srl.apply(dgraph)
    this.extract(dgraph)(frames)
  }

  def extract(dgraph: DependencyGraph)(frames: Seq[Frame]) = {
    val hierarchy = FrameHierarchy.fromFrames(dgraph, frames).toSeq
    hierarchy.flatMap { hierarchy =>
      val extrs = SrlExtraction.fromFrameHierarchy(dgraph)(hierarchy)
      extrs.map { extr => SrlExtractionInstance(extr, hierarchy, dgraph) }
    }
  }
}

object SrlExtractor extends App {
  sealed abstract class OutputFormat
  object OutputFormat {
    def apply(format: String): OutputFormat = {
      format.toLowerCase match {
        case "standard" => Standard
        case "annotation" => Annotation
        case "evaluation" => Evaluation
        case _ => throw new IllegalArgumentException("Unknown output format: " + format)
      }
    }

    case object Standard extends OutputFormat
    case object Annotation extends OutputFormat
    case object Evaluation extends OutputFormat
  }

  case class Config(inputFile: Option[File] = None,
      outputFile: Option[File] = None,
      outputFormat: OutputFormat = OutputFormat.Standard,
      gold: Map[String, Boolean] = Map.empty,
      classifierUrl: URL = SrlConfidenceFunction.defaultModelUrl) {
    def source() = {
      inputFile match {
        case Some(file) => Source.fromFile(file, "UTF8")
        case None => Source.stdin
      }
    }

    def writer() = {
      outputFile match {
        case Some(file) => new PrintWriter(file, "UTF8")
        case None => new PrintWriter(System.out)
      }
    }
  }

  val argumentParser = new scopt.immutable.OptionParser[Config]("srl-ie") {
    def options = Seq(
      argOpt("input file", "input file") { (string, config) =>
        val file = new File(string)
        require(file.exists, "input file does not exist: " + file)
        config.copy(inputFile = Some(file))
      },
      argOpt("ouput file", "output file") { (string, config) =>
        val file = new File(string)
        config.copy(outputFile = Some(file))
      },
      opt("gold", "gold file") { (string, config) =>
        val file = new File(string)
        require(file.exists, "gold file does not exist: " + file)
        val gold = Resource.using (Source.fromFile(file, "UTF8")) { source =>
          (for {
            line <- source.getLines
            (annotation, string) = line.split("\t") match {
              case Array(annotation, string, _ @ _*) => (annotation, string)
              case _ => throw new MatchError("Could not parse gold entry: " + line)
            }
            boolean = if (annotation == "1") true else false
          } yield {
            string -> boolean
          }).toMap
        }
        config.copy(gold = gold)
      },
      opt("classifier", "url to classifier model") { (string, config) =>
        val file = new File(string)
        require(file.exists, "classifier file does not exist: " + file)
        config.copy(classifierUrl = file.toURI.toURL)
      },
      opt("format", "output format: {standard, annotation, evaluation}") { (string, config) =>
        config.copy(outputFormat = OutputFormat(string))
      })
  }

  argumentParser.parse(args, Config()) match {
    case Some(config) => run(config)
    case None =>
  }

  def run(config: Config) {
    val parser = new ClearParser()
    val srl = new ClearSrl()
    val srlie = new SrlExtractor(srl)
    val conf = SrlConfidenceFunction.fromUrl(SrlFeatureSet, config.classifierUrl)

    def graphify(line: String) = {
      (Exception.catching(classOf[DependencyGraph.SerializationException]) opt DependencyGraph.deserialize(line)) match {
        case Some(graph) => graph
        case None => parser.dependencyGraph(line)
      }
    }

    Resource.using(config.source()) { source =>
      Resource.using(config.writer()) { writer =>
        Timing.timeThen {
          for (line <- source.getLines) {
            try {
              val graph = graphify(line)
              val insts = srlie.apply(graph)
              val triples = insts.flatMap(_.triplize(true))

              if (config.outputFormat == OutputFormat.Standard) {
                writer.println(graph.serialize)
                writer.println()

                val frames = srl(graph)
                writer.println("frames:")
                frames.map(_.serialize) foreach writer.println
                writer.println()

                val hierarchy = FrameHierarchy.fromFrames(graph, frames)
                writer.println("hierarchical frames:")
                hierarchy foreach writer.println
                writer.println()

                writer.println("extractions:")
                insts.foreach { inst =>
                  val score = conf(inst)
                  writer.println(("%1.2f" format score) + ": " + inst.extr)
                }
                writer.println()

                writer.println("triples:")
                triples.map(_.extr) foreach writer.println

                val transformations = insts.flatMap(_.extr.transformations(SrlExtraction.PassiveDobj))
                if (transformations.size > 0) {
                  writer.println("transformations:")
                  transformations foreach writer.println
                }

                writer.println()
              } else if (config.outputFormat == OutputFormat.Annotation) {
                for (inst <- triples) {
                  val extr = inst.extr
                  val string = extr.basicTripleString
                  writer.println(Iterable(config.gold.get(string).map(if (_) 1 else 0).getOrElse(""), string, extr.arg1, extr.relation, extr.arg2s.mkString("; "), line).mkString("\t"))
                }
              } else if (config.outputFormat == OutputFormat.Evaluation) {
                for (inst <- triples) {
                  val extr = inst.extr
                  val string = extr.basicTripleString
                  writer.println(Iterable(config.gold.get(string).map(if (_) 1 else 0).getOrElse(""), conf(inst), string, extr.arg1, extr.relation, extr.arg2s.mkString("; "), line).mkString("\t"))
                }
              }

              writer.flush()
            }
            catch {
              case e: Exception => e.printStackTrace()
            }
          }
        } { ns => System.err.println("Extractions in: " + Timing.Seconds.format(ns)) }
      }
    }
  }
}
