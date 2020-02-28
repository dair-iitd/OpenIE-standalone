/**
 * 
 */
package edu.iitd.cse.open_nre.onre.runner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.iitd.cse.open_nre.onre.OnreGlobals;
import edu.iitd.cse.open_nre.onre.constants.OnreConstants;
import edu.iitd.cse.open_nre.onre.domain.OnreExtraction;
import edu.iitd.cse.open_nre.onre.domain.OnrePatternNode;
import edu.iitd.cse.open_nre.onre.domain.OnrePatternTree;
import edu.iitd.cse.open_nre.onre.domain.Onre_dsDanrothSpans;
import edu.iitd.cse.open_nre.onre.helper.MayIHelpYou;
import edu.iitd.cse.open_nre.onre.helper.OnreHelper_DanrothQuantifier;
import edu.iitd.cse.open_nre.onre.helper.OnreHelper_json;
import edu.iitd.cse.open_nre.onre.helper.OnreHelper_pattern;
import edu.iitd.cse.open_nre.onre.utils.OnreIO;
import edu.iitd.cse.open_nre.onre.utils.OnreUtils;


/**
 * @author harinder
 *
 */
public class Onre_runMe_jsonStrings {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Onre_runMe.setArguments(args);
		
		File folder = new File(args[1]);
		
		Set<String> files = new TreeSet<>();
		OnreUtils.listFilesForFolder(folder, files);
		
		List<OnreExtraction> extrs_all = new ArrayList<OnreExtraction>();
		Map<String, String> fact_extrs_all = new HashMap<String, String>();
		for (String file : files) {
			if(!file.endsWith(OnreConstants.SUFFIX_JSON_STRINGS)) continue; //only jsonSuffix files are required
			System.out.println("----------------------------------running file: " + file);
			
			List<String> inputJsonStrings_patternTree = OnreIO.readFile(file);
			List<Onre_dsDanrothSpans> listOfDanrothSpans = OnreHelper_DanrothQuantifier.getListOfDanrothSpans(file.replaceAll("_jsonStrings", ""));

			List<OnrePatternNode> list_configuredPattern = OnreHelper_pattern.getDepPatterns();
			
			for(int i=0;i<inputJsonStrings_patternTree.size();i++) {
				OnrePatternTree onrePatternTree = OnreHelper_json.getOnrePatternTree(inputJsonStrings_patternTree.get(i));
				Map<OnreExtraction, Integer> extrs = MayIHelpYou.runMe(onrePatternTree, listOfDanrothSpans.get(i), list_configuredPattern);
				
				if(extrs == null) continue;
				
				Map<String, Integer> uniq_extrs = new HashMap<String, Integer>();
				for(Map.Entry<OnreExtraction, Integer> entry : extrs.entrySet()) {
					OnreExtraction extr = entry.getKey();
					
					String extr_string = extr.toString();
					Integer newValue = entry.getValue();
					
					//taking the highest(lowest number) patternNumber
					Integer currValue = uniq_extrs.get(extr_string);
					if(currValue!=null && currValue<newValue) newValue=currValue;
					
					uniq_extrs.put(extr_string, newValue);
				}
				
				uniq_extrs = OnreUtils.sortMapByValue(uniq_extrs, false);
				
				if(!OnreGlobals.arg_onre_isSeedFact) {
					System.out.println("::" + (i+1));
					System.out.println(onrePatternTree.sentence);
					for (Map.Entry<String, Integer> entry : uniq_extrs.entrySet()) {
						System.out.println(entry.getValue()-OnreConstants.NUMBER_OF_SEED_PATTERNS);
						System.out.println(entry.getKey());
					}
					System.out.println();
				}
				extrs_all.addAll(extrs.keySet());
			}
		}
		
		if(OnreGlobals.arg_onre_isSeedFact) OnreIO.writeFile(args[1]+"_out_facts_newSeedFactsLogic_JustFacts", extrs_all);		
	}

}
