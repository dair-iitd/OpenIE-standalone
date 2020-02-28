/**
 * 
 */
package edu.iitd.cse.open_nre.onre.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.iitd.cse.open_nre.onre.constants.OnreConstants;
import edu.iitd.cse.open_nre.onre.domain.OnrePatternNode;
import edu.iitd.cse.open_nre.onre.domain.Onre_dsDanrothSpan;
import edu.iitd.cse.open_nre.onre.domain.Onre_dsDanrothSpans;
import edu.iitd.cse.open_nre.onre.utils.OnreIO;
import edu.iitd.cse.open_nre.onre.utils.OnreUtils_number;
import edu.iitd.cse.open_nre.onre.utils.OnreUtils_string;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import edu.illinois.cs.cogcomp.quant.standardize.Quantity;

/**
 * @author harinder
 *
 */
public class OnreHelper_DanrothQuantifier {
	
	private static Double getQuantityValue(QuantSpan quantSpan) {
		return ((Quantity)(quantSpan.object)).value;
	}
	
	private static String getQuantityPhrase(QuantSpan quantSpan) {
		return ((Quantity)(quantSpan.object)).phrase;
	}
	
	private static String getQuantityUnit(QuantSpan quantSpan) {
		return ((Quantity)(quantSpan.object)).units;
	}
	
	private static String getQuantityBound(QuantSpan quantSpan) {
		return ((Quantity)(quantSpan.object)).bound;
	}
	
	public static String getPhraseExceptValue(String phrase) {
		String value = getValueFromPhrase(phrase);
		if(value == null) return phrase;
		
		phrase =  phrase.replaceAll(".*"+value, "");
		phrase = phrase.replace("  ", " ");
		return phrase.trim();
	}
	
	public static String getValueFromPhrase(String phrase) {
		String[] phraseSplit = phrase.split(" "); //TODO: need tokenization?
		for (String word : phraseSplit) {
			if(OnreUtils_number.isNumber(word)) return word;
		}
		
		return null;
	}
	
	private static String getUnitFromPhrase(String phrase, String unit) {
		if(unit.split(" ").length>1) {
			if(phrase.contains(unit)) return unit; //will be used only in case of type5
			return null;
		}
		
		String[] phraseSplit = phrase.split(" "); //TODO: need tokenization?
		for (String word : phraseSplit) {
			if(unit.equalsIgnoreCase("us$")) {
				if(word.equalsIgnoreCase("dollar")) return "dollar";
				if(word.contains("$")) return "$";
				if(word.equalsIgnoreCase("cents")) return "cents";
			}
			
			if(unit.equalsIgnoreCase("percent")) {
				if(word.contains("%")) return "%";
			}
			
			if(word.equalsIgnoreCase(unit)) return unit;
		}
		
		return null;
	}
	
	public static Map<Double, String> getValueMap(String text, Onre_dsDanrothSpans danrothSpans) {
		Map<Double, String> map_quantifiers_value = new HashMap<Double, String>();
		
		for (Onre_dsDanrothSpan danrothSpan : danrothSpans.quantSpans) {
			String valueFromPhrase = OnreUtils_string.lowerTrim(getValueFromPhrase(danrothSpan.phrase));
			map_quantifiers_value.put(danrothSpan.value, valueFromPhrase);
		}
		
		return map_quantifiers_value;
	}
	
	public static Map<String, String> getUnitMap(String text, Onre_dsDanrothSpans danrothSpans) {
		Map<String, String> map_quantifiers_unit = new HashMap<String, String>();
		
		for (Onre_dsDanrothSpan danrothSpan : danrothSpans.quantSpans) {
			String unitFromPhrase = OnreUtils_string.lowerTrim(getUnitFromPhrase(danrothSpan.phrase, danrothSpan.unit));
			map_quantifiers_unit.put(OnreUtils_string.replacePer_centToPerCent(danrothSpan.unit), unitFromPhrase);
		}
		
		return map_quantifiers_unit;
	}

	public static Map<String, Onre_dsDanrothSpan> getUnitDanrothMap(String text, Onre_dsDanrothSpans danrothSpans) {
		Map<String, Onre_dsDanrothSpan> map_quantifiers_unitDanroth = new HashMap<String, Onre_dsDanrothSpan>();
		
		for (Onre_dsDanrothSpan danrothSpan : danrothSpans.quantSpans) {
			map_quantifiers_unitDanroth.put(OnreUtils_string.replacePer_centToPerCent(danrothSpan.unit), danrothSpan);
		}
		
		return map_quantifiers_unitDanroth;
	}
	
	public static Onre_dsDanrothSpans getQuantitiesDanroth(String text) {
		List<QuantSpan> quantSpans = null;

		Quantifier quantifier = new Quantifier();
		quantSpans = quantifier.getSpans(text, true);

		Onre_dsDanrothSpans danrothSpans = new Onre_dsDanrothSpans();
		
		for (QuantSpan quantSpan : quantSpans) {
			if(!(quantSpan.object instanceof Quantity)) continue;
			danrothSpans.quantSpans.add(getDanrothSpanFromQuantSpan(quantSpan));
		}
		
		return danrothSpans;
	}

	private static Onre_dsDanrothSpan getDanrothSpanFromQuantSpan(QuantSpan quantSpan) {
		Onre_dsDanrothSpan danrothSpan = new Onre_dsDanrothSpan();
		danrothSpan.phrase = OnreUtils_string.lowerTrim(getQuantityPhrase(quantSpan));
		danrothSpan.value = getQuantityValue(quantSpan);
		danrothSpan.bound = OnreUtils_string.lowerTrim(getQuantityBound(quantSpan));
		danrothSpan.unit = OnreUtils_string.lowerTrim(getQuantityUnit(quantSpan));
		
		danrothSpan.start = quantSpan.start;
		danrothSpan.end = quantSpan.end;
		
		return danrothSpan;
	}
	
	public static Onre_dsDanrothSpan getQuantity(OnrePatternNode subTreeNode, Onre_dsDanrothSpans danrothSpans) {
		for (Onre_dsDanrothSpan danrothSpan : danrothSpans.quantSpans)
			if(subTreeNode.offset >= danrothSpan.start && subTreeNode.offset <= danrothSpan.end) return danrothSpan;
		
		for (Onre_dsDanrothSpan danrothSpan : danrothSpans.quantSpans)
			if(OnreUtils_string.isIgnoreCaseIgnoreCommaIgnoreSpaceContains(danrothSpan.phrase, subTreeNode.word)) return danrothSpan;
		
		return null;
	}
	
	public static List<Onre_dsDanrothSpans> getListOfDanrothSpans(String file) throws IOException {
		List<String> jsonDanrothSpans = OnreIO.readFile(file+OnreConstants.SUFFIX_DANROTH_SPANS);
		return getDanrothSpansFromJsonStrings(jsonDanrothSpans);
	}
	
	private static List<Onre_dsDanrothSpans> getDanrothSpansFromJsonStrings(List<String> jsonDanrothSpans) {
		List<Onre_dsDanrothSpans> listOfDanrothSpans = new ArrayList<>();
		
		for (String jsonDanrothSpan : jsonDanrothSpans) {
			Onre_dsDanrothSpans danrothSpans = (Onre_dsDanrothSpans)OnreHelper_json.getObjectFromJsonString(jsonDanrothSpan, Onre_dsDanrothSpans.class);
			listOfDanrothSpans.add(danrothSpans);
		}
		
		return listOfDanrothSpans;
	}
}
