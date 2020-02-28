/**
 * 
 */
package edu.iitd.cse.open_nre.onre;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import edu.iitd.cse.open_nre.onre.constants.Onre_dsRunType;

/**
 * @author harinder
 *
 */
public class OnreGlobals {
	
	public static String sentence;
	
	public static Gson gson; //note: don't use it directly...call OnreHelper_json.getGson() instead
	
	//arguments-onre
	public static boolean arg_onre_isSeedFact = false;
	public static boolean isSentenceInPastTense = false;
	public static boolean isMatchingWithBe = false;
	public static String negatedWord = null;
	public static String auxVerb = null;
	public static String expandedOnPrep = null;
	public static Map<String, String> singular_possessive_pronounsTOpronouns = new HashMap<String, String>(){{
		   put("my", "I");
		   put("its", "It");
		   put("his", "He");
		   put("her", "She");
		}};
	public static Map<String, String> plural_possessive_pronounsTOpronouns = new HashMap<String, String>(){{
		   put("your", "You");
		   put("our", "We");
		   put("your", "You");
		   put("their", "They");
		}};
	public static List<String> hasPronounsList;
	public static List<String> havePronounsList;
	public static List<String> auxVerbsList;
	
	//arguments-onreDS
	public static Onre_dsRunType arg_onreds_runType;
	public static String arg_onreds_path_inputFolder;
	public static String arg_onreds_path_facts;
	public static double arg_onreds_partialMatchingThresholdPercent;
	
	public static void resetGlobals() {
		isSentenceInPastTense = false;
		isMatchingWithBe = false;
		negatedWord = null;
		auxVerb = null;
		expandedOnPrep = null;
	}
}
