package edu.knowitall.srlie.confidence

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import edu.knowitall.tool.parse.ClearParser
import edu.knowitall.tool.srl.Frame
import edu.knowitall.tool.parse.graph.DependencyGraph
import edu.knowitall.srlie.SrlExtractor

@RunWith(classOf[JUnitRunner])
class SrlConfidenceFunctionTest extends Specification {
  val srl = new SrlExtractor(null)

  val sentence = "John, who ran a mile, ate corn on the cob."
  ("confidence function works on: '" + sentence + "'") in {
    val conf = SrlConfidenceFunction.loadDefaultClassifier()
    val dgraph = DependencyGraph.deserialize("punct(John_NNP_0_0, ,_,_1_4); rcmod(John_NNP_0_0, was_VBD_3_10); nsubj(was_VBD_3_10, who_WP_2_6); acomp(was_VBD_3_10, old_JJ_6_22); num(years_NNS_5_16, 5_CD_4_14); npadvmod(old_JJ_6_22, years_NNS_5_16); nsubj(ate_VBD_8_27, John_NNP_0_0); punct(ate_VBD_8_27, ,_,_7_25); dobj(ate_VBD_8_27, corn_NN_9_31); prep(ate_VBD_8_27, on_IN_10_36); punct(ate_VBD_8_27, ._._13_46); pobj(on_IN_10_36, cob_NN_12_43); det(cob_NN_12_43, the_DT_11_39)")
    val frames = IndexedSeq(
      "be_3.01:[A1=John_0, R-A1=who_2, A2=old_6]",
      "eat_8.01:[A0=John_0, A1=corn_9, AM-LOC=on_10]") map Frame.deserialize(dgraph)
    val insts = srl.synchronized {
      srl.extract(dgraph)(frames)
    }

    insts.map { inst => (inst, conf(inst)) } must not(throwA[Exception])
  }
}
