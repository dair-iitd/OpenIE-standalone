package edu.knowitall.srlie

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import edu.knowitall.tool.parse.ClearParser
import edu.knowitall.tool.srl.Frame
import edu.knowitall.tool.parse.graph.DependencyGraph
import edu.knowitall.tool.srl.FrameHierarchy
import edu.knowitall.srlie.nested.SrlNestedExtraction

@RunWith(classOf[JUnitRunner])
class SrlNestedExtractionTest extends Specification {
  val srl = new SrlExtractor(null)

  {
    val sentence = "Adaptation helps an organism survive in its environment."
    ("extractions from: " + sentence) in {
      val dgraph = DependencyGraph.deserialize("nsubj(helps_VBZ_1_11, Adaptation_NN_0_0); ccomp(helps_VBZ_1_11, survive_VB_4_29); det(organism_NN_3_20, an_DT_2_17); nsubj(survive_VB_4_29, organism_NN_3_20); prep(survive_VB_4_29, in_IN_5_37); pobj(in_IN_5_37, environment_NN_7_44); poss(environment_NN_7_44, its_PRP$_6_40)")
      val frames = Seq("help_1.01:[A0=Adaptation_0, A1=survive_4]", "survive_4.01:[A0=organism_3, AM-LOC=in_5]") map Frame.deserialize(dgraph)

      val extrs = srl.synchronized {
        srl.extract(dgraph)(frames).map(_.extr)
      }

      val nested = SrlNestedExtraction.from(extrs)

      nested.size == 1
    }
  }

  {
    val sentence = "A turtle eating worms is an example of taking in nutrients."
    ("extractions from: " + sentence) in {
      val dgraph = DependencyGraph.deserialize("det(turtle_NN_1_2, A_DT_0_0); partmod(turtle_NN_1_2, eating_VBG_2_9); dobj(eating_VBG_2_9, worms_NNS_3_16); nsubj(is_VBZ_4_22, turtle_NN_1_2); attr(is_VBZ_4_22, example_NN_6_28); punct(is_VBZ_4_22, ._._11_58); det(example_NN_6_28, an_DT_5_25); prep(example_NN_6_28, of_IN_7_36); pcomp(of_IN_7_36, taking_VBG_8_39); prep(taking_VBG_8_39, in_IN_9_46); pobj(in_IN_9_46, nutrients_NNS_10_49)")
      val frames = Seq("eat_2.01:[A0=turtle_1, A1=worms_3]", "be_4.01:[A1=turtle_1, A2=example_6]", "take_8.06:[AM-LOC=in_9]") map Frame.deserialize(dgraph)

      val extrs = srl.synchronized {
        srl.extract(dgraph)(frames).map(_.extr)
      }

      val nested = SrlNestedExtraction.from(extrs)

      nested.size == 1
    }
  }

  {
    val sentence = "A turtle eating worms helps the organism survive in its environment."
    ("extractions from: " + sentence) in {
      val dgraph = DependencyGraph.deserialize("det(turtle_NN_1_2, A_DT_0_0); partmod(turtle_NN_1_2, eating_VBG_2_9); dobj(eating_VBG_2_9, worms_NNS_3_16); nsubj(helps_VBZ_4_22, turtle_NN_1_2); ccomp(helps_VBZ_4_22, survive_VB_7_41); punct(helps_VBZ_4_22, ._._11_67); det(organism_NN_6_32, the_DT_5_28); nsubj(survive_VB_7_41, organism_NN_6_32); prep(survive_VB_7_41, in_IN_8_49); pobj(in_IN_8_49, environment_NN_10_56); poss(environment_NN_10_56, its_PRP$_9_52)")
      val frames = Seq("eat_2.01:[A0=turtle_1, A1=worms_3]", "help_4.01:[A0=turtle_1, A1=survive_7]", "survive_7.01:[A0=organism_6, AM-LOC=in_8]") map Frame.deserialize(dgraph)

      val extrs = srl.synchronized {
        srl.extract(dgraph)(frames).map(_.extr)
      }

      val nested = SrlNestedExtraction.from(extrs)

      nested.size == 1
    }
  }
}