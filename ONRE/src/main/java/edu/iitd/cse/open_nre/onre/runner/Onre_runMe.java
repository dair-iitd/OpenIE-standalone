/**
 * 
 */
package edu.iitd.cse.open_nre.onre.runner;

import java.io.IOException;
import java.util.Map;

import edu.iitd.cse.open_nre.onre.OnreGlobals;
import edu.iitd.cse.open_nre.onre.constants.OnreConstants;
import edu.iitd.cse.open_nre.onre.domain.OnreExtraction;
import edu.iitd.cse.open_nre.onre.helper.MayIHelpYou;
import edu.iitd.cse.open_nre.onre.helper.OnreHelper_graph;
import edu.knowitall.tool.parse.ClearParser;
import edu.knowitall.tool.parse.DependencyParser;
import edu.knowitall.tool.parse.graph.DependencyGraph;
import edu.knowitall.tool.postag.ClearPostagger;
import edu.knowitall.tool.tokenize.ClearTokenizer;


/**
 * @author harinder
 *
 */
public class Onre_runMe {
	
	static DependencyParser parser;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Onre_runMe.setArguments(args);

		//String sentence = "James Valley has 5 cubic miles of fruit orchards.";
		String sentence = "Home Depot founder";
		DependencyGraph depGraph = getDepGraph(sentence);
		Map<String, String> posTags = OnreHelper_graph.getPosTags(depGraph);
		
		System.out.println(sentence);
		System.out.println();
		if(depGraph != null) {
			Map<OnreExtraction, Integer> extrs = MayIHelpYou.runMe(depGraph);
			for (OnreExtraction onreExtraction : extrs.keySet()) {
				//System.out.println(onreExtraction.patternNumber-OnreConstants.NUMBER_OF_SEED_PATTERNS);
				System.out.println(onreExtraction);
			}
		}
	}
	
	public static void setArguments(String[] args) {
		if(args.length > 0) OnreGlobals.arg_onre_isSeedFact = (args[0].equals("true")); //TODO: "shall have named arguments"
	}

    public static DependencyGraph getDepGraph(String sentence) {
    	if(parser == null) parser = getParser();
		
		DependencyGraph depGraph = null;
		
		try {
		depGraph = parser.apply(sentence);
		} catch(AssertionError error) {
			System.err.println("----->" + error.toString());
			return null;
		}
		
	    return depGraph;
    }

	private static DependencyParser getParser() {
		ClearTokenizer tokenizer = new ClearTokenizer();
		ClearPostagger postagger = new ClearPostagger(tokenizer);
		DependencyParser parser = new ClearParser(postagger);
		return parser;
	}
}
