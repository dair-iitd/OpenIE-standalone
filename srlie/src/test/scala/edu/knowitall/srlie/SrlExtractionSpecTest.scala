package edu.knowitall.srlie

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import edu.knowitall.tool.parse.ClearParser
import edu.knowitall.tool.srl.Frame
import edu.knowitall.tool.parse.graph.DependencyGraph
import edu.knowitall.tool.srl.FrameHierarchy

@RunWith(classOf[JUnitRunner])
class SrlExtractionSpecTest extends Specification {
  val srl = new SrlExtractor(null)

  def expectedExtractions(sentence: String, dgraphString: String, frameStrings: Seq[String], expectedExtractions: Seq[String]) = {
    ("no errors with: '" + sentence + "'") in {
      val dgraph = DependencyGraph.deserialize(dgraphString)
      val frames = frameStrings map Frame.deserialize(dgraph)
      srl.synchronized {
        val insts = srl.extract(dgraph)(frames)
        val extrs = insts.map(_.extr)

        // make sure character offsets are correct for arguments (args must be contiguous)
        require(insts.forall(inst => inst.extr.arg1.text == inst.dgraph.text.substring(inst.extr.arg1.offsets.start, inst.extr.arg1.offsets.end)))
        require(insts.forall(inst => inst.extr.arg2s.forall(arg2 => arg2.text == inst.dgraph.text.substring(arg2.offsets.start, arg2.offsets.end))))

        extrs.map(_.toString) must haveTheSameElementsAs(expectedExtractions)
      }
    }
  }

  def expectedTriples(sentence: String, dgraphString: String, frameStrings: Seq[String], expectedTriples: Seq[String]) = {
    ("no errors with: '" + sentence + "'") in {
      val dgraph = DependencyGraph.deserialize(dgraphString)
      val frames = frameStrings map Frame.deserialize(dgraph)
      srl.synchronized {
        val triples = srl.extract(dgraph)(frames).flatMap(_.extr.triplize())
        triples.map(_.toString) must haveTheSameElementsAs(expectedTriples)
      }
    }
  }

  // val parser = new ClearParser()

  /*
  {
    val sentence = "John wants to fly to Boston this Thursday for a much needed vacation."
    sentence in {
      val extrs = srl.synchronized {
        val graph = parser.dependencyGraph(sentence)
        srl(graph)
      }

      val target = extrs.find(_.arg1.text == "John")
      target must beSome

      target.get.active must beTrue
    }
  }

  {
    val sentence = "Obama was criticized for his tax plan."
    sentence in {
      val extrs = srl.synchronized {
        val graph = parser.dependencyGraph(sentence)
        srl(graph)
      }

      val target = extrs.find(_.arg1.text == "Obama")
      target must beSome

      target.get.active must beFalse
    }
  }

  {
    val sentence = "Obama was criticized by Romney for his tax plan."
    sentence in {
      val extrs = srl.synchronized {
        val graph = parser.dependencyGraph(sentence)
        srl(graph)
      }

      val target = extrs.find(_.arg1.text == "Obama")
      target must beSome

      target.get.active must beFalse
    }
  }
  */

  {
    val sentence = "Alcohol is a drug, a sedative, which depresses the central nervous system"
    ("extractions from: " + sentence) in {
      val dgraph = DependencyGraph.deserialize("nsubj(is_VBZ_1_8, Alcohol_NN_0_0); attr(is_VBZ_1_8, drug_NN_3_13); det(drug_NN_3_13, a_DT_2_11); punct(drug_NN_3_13, ,_,_4_17); appos(drug_NN_3_13, sedative_NN_6_21); det(sedative_NN_6_21, a_DT_5_19); punct(sedative_NN_6_21, ,_,_7_29); rcmod(sedative_NN_6_21, depresses_VBZ_9_37); nsubj(depresses_VBZ_9_37, which_WDT_8_31); dobj(depresses_VBZ_9_37, system_NN_13_67); det(system_NN_13_67, the_DT_10_47); amod(system_NN_13_67, central_JJ_11_51); amod(system_NN_13_67, nervous_JJ_12_59)")
      val frames = Seq("be_1.01:[A1=Alcohol_0, A2=drug_3]", "depress_9.02:[A0=sedative_6, R=which_8, A1=system_13]") map Frame.deserialize(dgraph)

      val extrs = srl.synchronized {
        srl.extract(dgraph)(frames).map(_.extr)
      }

      extrs.size must_== 2
      extrs.map(_.toString) must haveTheSameElementsAs(Seq("(Alcohol; is; a drug)", "(a sedative; depresses; the central nervous system)"))
    }
  }

  {
    val sentence = "If Grandma had wheels, she will not be a tea trolley."
    ("extractions from: " + sentence) in {
      val dgraph = DependencyGraph.deserialize("mark(had_VBD_2_11, If_IN_0_0); nsubj(had_VBD_2_11, Grandma_NNP_1_3); dobj(had_VBD_2_11, wheels_NNS_3_15); advcl(be_VB_8_36, had_VBD_2_11); punct(be_VB_8_36, ,_,_4_21); nsubj(be_VB_8_36, she_PRP_5_23); aux(be_VB_8_36, will_MD_6_27); neg(be_VB_8_36, not_RB_7_32); attr(be_VB_8_36, trolley_NN_11_45); punct(be_VB_8_36, ._._12_52); det(trolley_NN_11_45, a_DT_9_39); nn(trolley_NN_11_45, tea_NN_10_41)")
      val frames = Seq("have_2.03:[A0=Grandma_1, A1=wheels_3]", "be_8.01:[AM-ADV=had_2, A1=she_5, AM-MOD=will_6, AM-NEG=not_7, A2=trolley_11]") map Frame.deserialize(dgraph)

      val extrs = srl.synchronized {
        srl.extract(dgraph)(frames).map(_.extr)
      }

      extrs.size must_== 2
      extrs.map(_.toString) must contain("(she; will not be; a tea trolley)")
    }
  }

  {
    val sentence = "John read a book in which philosophy was discussed."
    ("extractions from: " + sentence) in {
      val dgraph = DependencyGraph.deserialize("nsubj(read_VBD_1_5, John_NNP_0_0); dobj(read_VBD_1_5, book_NN_3_12); punct(read_VBD_1_5, ._._9_50); det(book_NN_3_12, a_DT_2_10); rcmod(book_NN_3_12, discussed_VBN_8_41); pcomp(in_IN_4_17, which_WDT_5_20); prep(discussed_VBN_8_41, in_IN_4_17); nsubjpass(discussed_VBN_8_41, philosophy_NN_6_26); auxpass(discussed_VBN_8_41, was_VBD_7_37)")
      val frames = Seq("read_1.01:[A0=John_0, A1=book_3]", "discuss_8.01:[AM-LOC=book_3, R-AM-LOC=in_4, A1=philosophy_6]") map Frame.deserialize(dgraph)

      val extrs = srl.synchronized {
        srl.extract(dgraph)(frames).map(_.extr)
      }

      extrs.size must_== 2
      extrs.map(_.toString) must contain("(philosophy; was discussed; L:a book)")
    }
  }

  {
    val sentence = "John lives in Orlando."
    ("extractions from: " + sentence) in {
      val dgraph = DependencyGraph.deserialize("nsubj(lives_VBZ_1_5, John_NNP_0_0); prep(lives_VBZ_1_5, in_IN_2_11); punct(lives_VBZ_1_5, ._._4_21); pobj(in_IN_2_11, Orlando_NNP_3_14)")
      val frames = Seq("live_1.01:[A0=John_0, AM-LOC=in_2]") map Frame.deserialize(dgraph)

      val extrs = srl.synchronized {
        srl.extract(dgraph)(frames).map(_.extr)
      }

      extrs.size must_== 1
      extrs.map(_.toString) must contain("(John; lives; L:in Orlando)")
    }
  }

  {
    val sentence = "John wants to blow his nose."
    ("extractions from: " + sentence) in {
      val dgraph = DependencyGraph.deserialize("nsubj(wants_VBZ_1_5, John_NNP_0_0); xcomp(wants_VBZ_1_5, blow_VB_3_14); punct(wants_VBZ_1_5, ._._6_27); aux(blow_VB_3_14, to_TO_2_11); dobj(blow_VB_3_14, nose_NN_5_23); poss(nose_NN_5_23, his_PRP$_4_19)")
      val frames = Seq("want_1.01:[A0=John_0, A1=blow_3]", "blow_3.09:[A0=John_0, A1=nose_5]") map Frame.deserialize(dgraph)

      val extrs = srl.synchronized {
        srl.extract(dgraph)(frames).map(_.extr)
      }

      extrs.size must_== 2
      extrs.map(_.toString) must haveTheSameElementsAs(Seq("(John; wants; to blow his nose)", "John wants:(John; wants to blow; his nose)"))
    }
  }

  {
    val sentence = "John wants to blow his nose and eat corn."
    ("no errors with: '" + sentence + "'") in {
      val dgraph = DependencyGraph.deserialize("nsubj(wants_VBZ_1_6, John_NNP_0_0); xcomp(wants_VBZ_1_6, blow_VB_3_15); punct(wants_VBZ_1_6, ._._9_41); aux(blow_VB_3_15, to_TO_2_12); dobj(blow_VB_3_15, nose_NN_5_24); cc(blow_VB_3_15, and_CC_6_29); conj(blow_VB_3_15, eat_VB_7_33); poss(nose_NN_5_24, his_PRP$_4_20); dobj(eat_VB_7_33, corn_NN_8_37)")
      val frames = IndexedSeq(
        "want_1.01:[A0=John_0, A1=blow_3]",
        "blow_3.09:[A0=John_0, A1=nose_5]",
        "eat_7.01:[A0=John_0, A1=corn_8]") map Frame.deserialize(dgraph)

      val extrs = srl.synchronized {
        srl.extract(dgraph)(frames).map(_.extr)
      }

      extrs.map(_.toString) must haveTheSameElementsAs(Seq("(John; wants; to blow his nose and eat corn)", "John wants:(John; wants to blow; his nose)", "John wants:(John; wants to eat; corn)"))
      extrs.find(_.toString == "John wants:(John; wants to blow; his nose)").get.context.map(_.text) must_== Some("John wants")
      extrs.find(_.toString == "John wants:(John; wants to eat; corn)").get.context.map(_.text) must_== Some("John wants")
    }
  }

  {
    val sentence = "Meteorites are the oldest rocks found on Earth."
    ("no errors with: '" + sentence + "'") in {
      val dgraph = DependencyGraph.deserialize("nsubj(are_VBP_1_11, Meteorites_NNPS_0_0); attr(are_VBP_1_11, rocks_NNS_4_26); punct(are_VBP_1_11, ._._8_46); det(rocks_NNS_4_26, the_DT_2_15); amod(rocks_NNS_4_26, oldest_JJS_3_19); partmod(rocks_NNS_4_26, found_VBN_5_32); prep(found_VBN_5_32, on_IN_6_38); pobj(on_IN_6_38, Earth_NNP_7_41)")
      val frames = IndexedSeq(
        "be_1.01:[A1=Meteorites_0, A2=rocks_4]",
        "bind_5.01:[A1=rocks_4, AM-LOC=on_6]") map Frame.deserialize(dgraph)
      srl.synchronized {
        srl.extract(dgraph)(frames).map(_.extr) must not(throwA[Exception])
      }
    }
  }

  {
    val sentence = "John, who ran a mile, ate corn on the cob."
    ("no errors with: '" + sentence + "'") in {
      val dgraph = DependencyGraph.deserialize("punct(John_NNP_0_0, ,_,_1_4); rcmod(John_NNP_0_0, was_VBD_3_10); nsubj(was_VBD_3_10, who_WP_2_6); acomp(was_VBD_3_10, old_JJ_6_22); num(years_NNS_5_16, 5_CD_4_14); npadvmod(old_JJ_6_22, years_NNS_5_16); nsubj(ate_VBD_8_27, John_NNP_0_0); punct(ate_VBD_8_27, ,_,_7_25); dobj(ate_VBD_8_27, corn_NN_9_31); prep(ate_VBD_8_27, on_IN_10_36); punct(ate_VBD_8_27, ._._13_46); pobj(on_IN_10_36, cob_NN_12_43); det(cob_NN_12_43, the_DT_11_39)")
      val frames = IndexedSeq(
         "be_3.01:[A1=John_0, R-A1=who_2, A2=old_6]",
         "eat_8.01:[A0=John_0, A1=corn_9, AM-LOC=on_10]") map Frame.deserialize(dgraph)
      srl.synchronized {
        srl.extract(dgraph)(frames).map(_.extr).map(_.toString) must haveTheSameElementsAs(List("(John; was; 5 years old)", "(John; ate; corn; L:on the cob)"))
      }
    }
  }

  {
    val sentence = "NJ threw the ball to Michae on Friday after work."
    ("no errors with: '" + sentence + "'") in {
      val dgraph = DependencyGraph.deserialize("nsubj(threw_VBD_1_3, NJ_NNP_0_0); dobj(threw_VBD_1_3, ball_NN_3_13); prep(threw_VBD_1_3, to_TO_4_18); prep(threw_VBD_1_3, on_IN_6_28); prep(threw_VBD_1_3, after_IN_8_38); punct(threw_VBD_1_3, ._._10_48); det(ball_NN_3_13, the_DT_2_9); pobj(to_TO_4_18, Michae_NNP_5_21); pobj(on_IN_6_28, Friday_NNP_7_31); pobj(after_IN_8_38, work_NN_9_44)")
      val frames = IndexedSeq(
         "throw_1.01:[A0=NJ_0, A1=ball_3, A2=to_4, AM-TMP=on_6, AM-TMP=after_8]") map Frame.deserialize(dgraph)
      srl.synchronized {
        val extrs = srl.extract(dgraph)(frames).map(_.extr)
        extrs.flatMap(_.triplize(true)).map(_.toString) must haveTheSameElementsAs(List("(NJ; threw; the ball)", "(NJ; threw the ball to; Michae)", "(NJ; threw the ball on; T:Friday)", "(NJ; threw the ball after; T:work)"))
        extrs.flatMap(_.triplize(false)).map(_.toString) must haveTheSameElementsAs(List("(NJ; threw; the ball)", "(NJ; threw to; Michae)", "(NJ; threw on; T:Friday)", "(NJ; threw after; T:work)"))
      }
    }
  }

  {
    val sentence = "Cordis sold its pacemaker operations two years ago to Telectronics Holding Ltd. of Australia ."
    ("no errors with: '" + sentence + "'") in {
      val dgraph = DependencyGraph.deserialize("nsubj(sold_VBD_1_7, Cordis_NNP_0_0); dobj(sold_VBD_1_7, operations_NNS_4_26); advmod(sold_VBD_1_7, ago_RB_7_47); prep(sold_VBD_1_7, to_IN_8_51); punct(sold_VBD_1_7, ._._14_93); poss(operations_NNS_4_26, its_PRP$_2_12); nn(operations_NNS_4_26, pacemaker_NN_3_16); num(years_NNS_6_41, two_CD_5_37); npadvmod(ago_RB_7_47, years_NNS_6_41); pobj(to_IN_8_51, Ltd._NNP_11_75); nn(Ltd._NNP_11_75, Telectronics_NNP_9_54); nn(Ltd._NNP_11_75, Holding_NNP_10_67); prep(Ltd._NNP_11_75, of_IN_12_80); pobj(of_IN_12_80, Australia_NNP_13_83)")
      val frames = IndexedSeq("sell_1.01:[A0=Cordis_0, A1=operations_4, AM-TMP=ago_7, A2=to_8]") map Frame.deserialize(dgraph)
      srl.synchronized {
        val extrs = srl.extract(dgraph)(frames).map(_.extr)
        extrs.map(_.toString) must haveTheSameElementsAs(List("(Cordis; sold; its pacemaker operations; T:two years ago; to Telectronics Holding Ltd. of Australia)"))
        val transformations = extrs.flatMap(_.transformations(SrlExtraction.PassiveDobj))
        transformations.map(_.toString) must haveTheSameElementsAs(List("(its pacemaker operations; [was] sold; T:two years ago; to Telectronics Holding Ltd. of Australia)"))
        val triples = transformations.flatMap(_.triplize(true))
        triples.map(_.toString) must haveTheSameElementsAs(List("(its pacemaker operations; [was] sold to Telectronics Holding Ltd. of Australia; T:two years ago)", "(its pacemaker operations; [was] sold to; Telectronics Holding Ltd. of Australia)"))
      }
    }
  }

    {
    val sentence = "Ford said owners should return the cars to dealers so the windshields can be removed and securely reinstalled ."
    ("no errors with: '" + sentence + "'") in {
      val dgraph = DependencyGraph.deserialize("nsubj(said_VBD_1_5, Ford_NNP_0_0); ccomp(said_VBD_1_5, removed_VBN_14_77); punct(said_VBD_1_5, ._._18_110); nsubj(return_VB_4_24, owners_NNS_2_10); aux(return_VB_4_24, should_MD_3_17); dobj(return_VB_4_24, cars_NNS_6_35); prep(return_VB_4_24, to_IN_7_40); det(cars_NNS_6_35, the_DT_5_31); pobj(to_IN_7_40, dealers_NNS_8_43); det(windshields_NNS_11_58, the_DT_10_54); ccomp(removed_VBN_14_77, return_VB_4_24); dep(removed_VBN_14_77, so_IN_9_51); nsubjpass(removed_VBN_14_77, windshields_NNS_11_58); aux(removed_VBN_14_77, can_MD_12_70); auxpass(removed_VBN_14_77, be_VB_13_74); cc(removed_VBN_14_77, and_CC_15_85); conj(removed_VBN_14_77, reinstalled_VBN_17_98); advmod(reinstalled_VBN_17_98, securely_RB_16_89)")
      val frames = IndexedSeq("say_1.01:[A0=Ford_0, A1=removed_14]", "return_4.02:[A0=owners_2, AM-MOD=should_3, A1=cars_6, A2=to_7]", "remove_14.01:[A1=windshields_11, AM-MOD=can_12]", "reinstall_17.01:[A1=windshields_11, AM-MOD=can_12, AM-MNR=securely_16]") map Frame.deserialize(dgraph)
      srl.synchronized {
        val extrs = srl.extract(dgraph)(frames).map(_.extr)
        extrs.map(_.toString) must haveTheSameElementsAs(List("(the windshields; securely reinstalled; )", "(Ford; said; owners should return the cars to dealers so the windshields can be removed and securely reinstalled)", "Ford said:(the windshields; can be removed; )", "Ford said the windshields can be removed:(owners; should return; the cars; to dealers)"))
        val transformations = extrs.flatMap(_.transformations(SrlExtraction.PassiveDobj))
        transformations.map(_.toString) must beEmpty
      }
    }
  }

  expectedExtractions(
    sentence = "Microsoft plans on filing a lawsuit against Google in New York.",
    dgraphString = "nsubj(plans_VBZ_1_10, Microsoft_NNP_0_0); prep(plans_VBZ_1_10, on_IN_2_16); punct(plans_VBZ_1_10, ._._11_62); pcomp(on_IN_2_16, filing_VBG_3_19); dobj(filing_VBG_3_19, lawsuit_NN_5_28); det(lawsuit_NN_5_28, a_DT_4_26); prep(lawsuit_NN_5_28, against_IN_6_36); pobj(against_IN_6_36, Google_NNP_7_44); prep(Google_NNP_7_44, in_IN_8_51); pobj(in_IN_8_51, York_NNP_10_58); nn(York_NNP_10_58, New_NNP_9_54)",
    frameStrings = Seq("plan_1.01:[A0=Microsoft_0, A1=on_2]", "file_3.01:[A0=Microsoft_0, A1=lawsuit_5, A3=against_6]"),
    expectedExtractions = Seq(
        "(Microsoft; plans; on filing a lawsuit against Google in New York)",
        "Microsoft plans:(Microsoft; plans on filing; a lawsuit; against Google in New York)"))


  expectedExtractions(
    sentence = "That mis-characterizes when Shark said he loves ham.",
    dgraphString = """nsubj(characterizes_VBZ_3_9, That_DT_0_0); hmod(characterizes_VBZ_3_9, mis_NN_1_5); hyph(characterizes_VBZ_3_9, -_HYPH_2_8); advcl(characterizes_VBZ_3_9, said_VBD_6_34); punct(characterizes_VBZ_3_9, ._._10_51); advmod(said_VBD_6_34, when_WRB_4_23); nsubj(said_VBD_6_34, Shark_NNP_5_28); ccomp(said_VBD_6_34, loves_VBZ_8_42); nsubj(loves_VBZ_8_42, he_PRP_7_39); dobj(loves_VBZ_8_42, ham_NN_9_48)""",
    frameStrings = Seq("characterize_3.01:[A0=That_0, AM-TMP=said_6]", "say_6.01:[R-AM-TMP=when_4, A0=Shark_5, A1=loves_8]", "love_8.01:[A0=he_7, A1=ham_9]"),
    expectedExtractions = Seq(
        "(That; characterizes; T:when Shark said he loves ham)",
        "(Shark; said; he loves ham)",
        "Shark said:(he; loves; ham)"))

  expectedExtractions(
    sentence = "Suharto moved his hands and spoke in a whisper today in what doctors called a miraculous recovery.",
    dgraphString = """nsubj(moved_VBD_1_8, Suharto_NNP_0_0); dobj(moved_VBD_1_8, hands_NNS_3_18); cc(moved_VBD_1_8, and_CC_4_24); conj(moved_VBD_1_8, spoke_VBD_5_28); punct(moved_VBD_1_8, ._._17_97); poss(hands_NNS_3_18, his_PRP$_2_14); prep(spoke_VBD_5_28, in_IN_6_34); npadvmod(spoke_VBD_5_28, today_NN_9_47); prep(spoke_VBD_5_28, in_IN_10_53); pobj(in_IN_6_34, whisper_NN_8_39); det(whisper_NN_8_39, a_DT_7_37); pcomp(in_IN_10_53, called_VBD_13_69); dobj(called_VBD_13_69, what_WP_11_56); nsubj(called_VBD_13_69, doctors_NNS_12_61); oprd(called_VBD_13_69, recovery_NN_16_89); det(recovery_NN_16_89, a_DT_14_76); amod(recovery_NN_16_89, miraculous_JJ_15_78)""",
    frameStrings = Seq("move_1.01:[A0=Suharto_0, A1=hands_3]", "speak_5.01:[A0=Suharto_0, AM-LOC=in_6, AM-TMP=today_9, AM-LOC=in_10]", "call_13.01:[R-A1=what_11, A0=doctors_12, A2=recovery_16]"),
    expectedExtractions = Seq(
        "(Suharto; moved; his hands)",
        "(Suharto; spoke; L:in a whisper; T:today; L:in what doctors called a miraculous recovery)",
        "(doctors; called; a miraculous recovery)"))

        /*
  expectedExtractions(
    sentence = "It was only when Jesus was in the boat and shouted , Peace be still that the disciples were saved from that terrible storm on the Sea of Galilee .",
    dgraphString = "nsubj(was_VBD_1_3, It_PRP_0_0); advcl(was_VBD_1_3, was_VBD_5_23); punct(was_VBD_1_3, ._._29_145); advmod(was_VBD_5_23, when_WRB_3_12); nsubj(was_VBD_5_23, Jesus_NNP_4_17); prep(was_VBD_5_23, in_IN_6_27); cc(was_VBD_5_23, and_CC_9_39); conj(was_VBD_5_23, shouted_VBD_10_43); pobj(in_IN_6_27, boat_NN_8_34); det(boat_NN_8_34, the_DT_7_30); punct(shouted_VBD_10_43, ,_,_11_51); ccomp(shouted_VBD_10_43, be_VB_13_59); advmod(be_VB_13_59, only_RB_2_7); nsubj(be_VB_13_59, Peace_NN_12_53); advmod(be_VB_13_59, still_RB_14_62); ccomp(be_VB_13_59, saved_VBN_19_92); det(disciples_NNS_17_77, the_DT_16_73); complm(saved_VBN_19_92, that_IN_15_68); nsubjpass(saved_VBN_19_92, disciples_NNS_17_77); auxpass(saved_VBN_19_92, were_VBD_18_87); prep(saved_VBN_19_92, from_IN_20_98); pobj(from_IN_20_98, storm_NN_23_117); det(storm_NN_23_117, that_DT_21_103); amod(storm_NN_23_117, terrible_JJ_22_108); prep(storm_NN_23_117, on_IN_24_123); pobj(on_IN_24_123, Sea_NNP_26_130); det(Sea_NNP_26_130, the_DT_25_126); prep(Sea_NNP_26_130, of_IN_27_134); pobj(of_IN_27_134, Galilee_NNP_28_137)",
    frameStrings = Seq("be_1.01:[A1=It_0, A2=was_5]",
      "be_5.01:[R-AM-TMP=when_3, A1=Jesus_4, A2=in_6]",
      "shout_10.01:[R-AM-TMP=when_3, A0=Jesus_4, A1=be_13]",
      "be_13.01:[AM-ADV=only_2, A1=Peace_12, AM-TMP=still_14, A2=saved_19]",
      "save_19.02:[A1=disciples_17, A2=from_20]"),
    expectedExtractions = Seq(
        "(It; was; only when Jesus was in the boat and shouted , Peace be still that the disciples were saved from that terrible storm on the Sea of Galilee)",
        "It was:(Jesus; shouted; Peace be still that the disciples were saved from that terrible storm on the Sea of Galilee)"))
        */

  expectedExtractions(
    sentence = "In 2005 , Gruner + Jahr exited the U.S. magazine business.",
    dgraphString = "pobj(In_IN_0_0, 2005_CD_1_3); nn(Jahr_NNP_5_19, Gruner_NNP_3_10); nn(Jahr_NNP_5_19, +_NNP_4_17); prep(exited_VBD_6_24, In_IN_0_0); punct(exited_VBD_6_24, ,_,_2_8); nsubj(exited_VBD_6_24, Jahr_NNP_5_19); dobj(exited_VBD_6_24, business_NN_10_49); punct(exited_VBD_6_24, ._._11_57); det(business_NN_10_49, the_DT_7_31); nn(business_NN_10_49, U.S._NNP_8_35); nn(business_NN_10_49, magazine_NN_9_40)",
    frameStrings = Seq("exit_6.01:[AM-TMP=In_0, A0=Jahr_5, A1=business_10]"),
    expectedExtractions = Seq(
        "(Gruner + Jahr; exited; the U.S. magazine business; T:In 2005)"))

  expectedTriples(
    sentence = "The president asked Americans to imagine the suicide terrorists who attacked the United States if they had been armed by Iraq .",
    dgraphString = "det(president_NN_1_4, The_DT_0_0); nsubj(asked_VBD_2_14, president_NN_1_4); dobj(asked_VBD_2_14, Americans_NNPS_3_20); xcomp(asked_VBD_2_14, imagine_VB_5_33); punct(asked_VBD_2_14, ._._21_126); aux(imagine_VB_5_33, to_TO_4_30); dobj(imagine_VB_5_33, terrorists_NNS_8_53); det(terrorists_NNS_8_53, the_DT_6_41); nn(terrorists_NNS_8_53, suicide_NN_7_45); rcmod(terrorists_NNS_8_53, attacked_VBD_10_68); nsubj(attacked_VBD_10_68, who_WP_9_64); dobj(attacked_VBD_10_68, States_NNP_13_88); advcl(attacked_VBD_10_68, armed_VBN_18_112); det(States_NNP_13_88, the_DT_11_77); nn(States_NNP_13_88, United_NNP_12_81); mark(armed_VBN_18_112, if_IN_14_95); nsubjpass(armed_VBN_18_112, they_PRP_15_98); aux(armed_VBN_18_112, had_VBD_16_103); auxpass(armed_VBN_18_112, been_VBN_17_107); agent(armed_VBN_18_112, by_IN_19_118); pobj(by_IN_19_118, Iraq_NNP_20_121)",
    frameStrings = Seq("ask_2.02:[A0=president_1, A2=Americans_3, A1=imagine_5]",
      "imagine_5.01:[A0=Americans_3, A1=terrorists_8]",
      "attack_10.01:[A0=terrorists_8, R-A0=who_9, A1=States_13, AM-ADV=armed_18]",
      "arm_18.01:[A1=they_15, A0=by_19]"),
    expectedTriples = Seq("(The president; asked Americans to; imagine the suicide terrorists)",
      "(Americans; to imagine; the suicide terrorists who attacked the United States)",
      "(the suicide terrorists; attacked; the United States)",
      "(they; had been armed by; Iraq)",
      "(The president; asked; Americans)"))

  expectedExtractions(
    sentence = "Thus , the phloem can serve some people , allowing for swift electrical communication between widely separated organs .",
    dgraphString = "det(phloem_NN_3_11, the_DT_2_7); advmod(serve_VB_5_22, Thus_RB_0_0); punct(serve_VB_5_22, ,_,_1_5); nsubj(serve_VB_5_22, phloem_NN_3_11); aux(serve_VB_5_22, can_MD_4_18); dobj(serve_VB_5_22, people_NNS_7_33); punct(serve_VB_5_22, ,_,_8_40); advcl(serve_VB_5_22, allowing_VBG_9_42); punct(serve_VB_5_22, ._._18_118); det(people_NNS_7_33, some_DT_6_28); prep(allowing_VBG_9_42, for_IN_10_51); pobj(for_IN_10_51, communication_NN_13_72); amod(communication_NN_13_72, swift_JJ_11_55); amod(communication_NN_13_72, electrical_JJ_12_61); prep(communication_NN_13_72, between_IN_14_86); pobj(between_IN_14_86, organs_NNS_17_111); advmod(separated_VBN_16_101, widely_RB_15_94); amod(organs_NNS_17_111, separated_VBN_16_101)",
    frameStrings = Seq("serve_5.01:[AM-DIS=Thus_0, A0=phloem_3, AM-MOD=can_4, A2=people_7, AM-ADV=allowing_9]", "allow_9.01:[A0=phloem_3, A1=for_10]", "separate_16.01:[AM-MNR=widely_15, A1=organs_17]"),
    expectedExtractions = Seq("(the phloem; can serve; some people)", "the phloem can serve:(the phloem; can serve some people allowing; for swift electrical communication between widely separated organs)"))

  expectedExtractions(
    sentence = "John said Frank works in the yard.",
    dgraphString = "nsubj(said_VBD_1_5, John_NNP_0_0); ccomp(said_VBD_1_5, works_VBZ_3_16); punct(said_VBD_1_5, ._._7_33); nsubj(works_VBZ_3_16, Frank_NNP_2_10); prep(works_VBZ_3_16, in_IN_4_22); pobj(in_IN_4_22, yard_NN_6_29); det(yard_NN_6_29, the_DT_5_25)",
    frameStrings = Seq("ay_1.01:[A0=John_0, A1=works_3]",
      "work_3.01:[A0=Frank_2, AM-LOC=in_4]"),
    expectedExtractions = Seq("(John; said; Frank works in the yard)", "John said:(Frank; works; L:in the yard)"))

  expectedTriples(
    sentence = "John gave the ball to Paul.",
    dgraphString = "nsubj(gave_VBD_1_5, John_NNP_0_0); dobj(gave_VBD_1_5, ball_NN_3_14); prep(gave_VBD_1_5, to_IN_4_19); punct(gave_VBD_1_5, ._._6_26); det(ball_NN_3_14, the_DT_2_10); pobj(to_IN_4_19, Paul_NNP_5_22)",
    frameStrings = Seq("give_1.01:[A0=John_0, A1=ball_3, A2=to_4]"),
    expectedTriples = Seq("(John; gave the ball to; Paul)", "(John; gave; the ball)"))

  expectedTriples(
    sentence = "Many of the defendants were also ordered to forfeit sums of money ranging from $100,000 to $2,000,000 as proceeds of their unlawful drug trafficking activities.",
    dgraphString = "prep(Many_JJ_0_0, of_IN_1_5); pobj(of_IN_1_5, defendants_NNS_3_12); det(defendants_NNS_3_12, the_DT_2_8); nsubjpass(ordered_VBN_6_33, Many_JJ_0_0); auxpass(ordered_VBN_6_33, were_VBD_4_23); advmod(ordered_VBN_6_33, also_RB_5_28); prep(ordered_VBN_6_33, to_IN_7_41); punct(ordered_VBN_6_33, ._._27_159); pobj(to_IN_7_41, sums_NNS_9_52); amod(sums_NNS_9_52, forfeit_JJ_8_44); prep(sums_NNS_9_52, of_IN_10_57); partmod(sums_NNS_9_52, ranging_VBG_12_66); pobj(of_IN_10_57, money_NN_11_60); prep(ranging_VBG_12_66, from_IN_13_74); pobj(from_IN_13_74, 2,000,000_CD_18_92); prep(from_IN_13_74, as_IN_19_102); quantmod(2,000,000_CD_18_92, $_$_14_79); number(2,000,000_CD_18_92, 100,000_CD_15_80); quantmod(2,000,000_CD_18_92, to_TO_16_88); quantmod(2,000,000_CD_18_92, $_$_17_91); pobj(as_IN_19_102, proceeds_NNS_20_105); prep(proceeds_NNS_20_105, of_IN_21_114); pobj(of_IN_21_114, activities_NNS_26_149); nn(trafficking_NN_25_137, drug_NN_24_132); poss(activities_NNS_26_149, their_PRP$_22_117); amod(activities_NNS_26_149, unlawful_JJ_23_123); nn(activities_NNS_26_149, trafficking_NN_25_137)",
    frameStrings = Seq("order_6.01:[A1=Many_0, AM-DIS=also_5, A2=to_7]", "range_12.01:[A1=sums_9, A3=from_13, A4=to_16]"),
    expectedTriples = Seq("(Many of the defendants; were ordered to; forfeit sums of money)", "(forfeit sums of money; ranging from; $100,000)"))

  expectedExtractions(
    sentence = "She is trying to get the Pope to proclaim that Mary is Co-Redemptrix .",
    dgraphString = "nsubj(trying_VBG_2_7, She_PRP_0_0); aux(trying_VBG_2_7, is_VBZ_1_4); xcomp(trying_VBG_2_7, get_VB_4_17); punct(trying_VBG_2_7, ._._13_69); aux(get_VB_4_17, to_TO_3_14); ccomp(get_VB_4_17, proclaim_VB_8_33); det(Pope_NNP_6_25, the_DT_5_21); nsubj(proclaim_VB_8_33, Pope_NNP_6_25); aux(proclaim_VB_8_33, to_TO_7_30); ccomp(proclaim_VB_8_33, is_VBZ_11_52); complm(is_VBZ_11_52, that_IN_9_42); nsubj(is_VBZ_11_52, Mary_NNP_10_47); attr(is_VBZ_11_52, Co-Redemptrix_NNP_12_55)",
    frameStrings = Seq("try_2.01:[A0=She_0, A1=get_4]",
      "get_4.04:[A0=She_0, A1=proclaim_8]",
      "proclaim_8.01:[A0=Pope_6, A1=is_11]",
      "be_11.01:[A1=Mary_10, A2=Co-Redemptrix_12]"),
    expectedExtractions = Seq("(She; is trying; to get the Pope to proclaim that Mary is Co-Redemptrix)",
      "She is trying:(She; is trying to get; the Pope to proclaim that Mary is Co-Redemptrix)",
      "She is trying to get:(the Pope; to proclaim; that Mary is Co-Redemptrix)",
      "She is trying to get the Pope to proclaim:(Mary; is; Co-Redemptrix)"))

  /*
  expectedExtractions(
    sentence = "I eat ice cream, John said.",
    dgraphString = "nsubj(eat_VBP_1_2, I_PRP_0_0); dobj(eat_VBP_1_2, cream_NN_3_10); nn(cream_NN_3_10, ice_NN_2_6); ccomp(said_VBD_6_22, eat_VBP_1_2); punct(said_VBD_6_22, ,_,_4_15); nsubj(said_VBD_6_22, John_NNP_5_17); punct(said_VBD_6_22, ._._7_26)",
    frameStrings = Seq("eat_1.01:[A0=I_0, A1=cream_3]", "say_6.01:[A1=eat_1, A0=John_5]"),
    expectedExtractions = Seq("(I eat ice cream; said; John)", "John said:(I; eat; ice cream)"))
  */
}
