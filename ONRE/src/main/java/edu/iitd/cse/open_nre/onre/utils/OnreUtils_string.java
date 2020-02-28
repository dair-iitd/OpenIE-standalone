package edu.iitd.cse.open_nre.onre.utils;


public class OnreUtils_string {
	
	public static boolean isIgnoreCaseMatch(String s1, String s2) {
		return s1.toLowerCase().matches(s2.toLowerCase());
	}
	
	public static boolean isIgnoreCaseContainsPhrase(String s1, String s2) {
		return s1.matches("(?i).*\\b" + s2 + "\\b.*");
	}
	
	// checks for substring match but the match must not be part of a word, rather should match word for word
	public static boolean isIgnoreCaseIgnoreCommaIgnoreSpaceContains(String s1, String s2) {
		if(!lowerTrimCommaSpace(s1).contains(lowerTrimCommaSpace(s2))) {
			return false;
		}
		else {
			int startIndex = s1.toLowerCase().indexOf(s2.toLowerCase());
			int endIndex = startIndex + s2.length() - 1;
			if(startIndex > 0 && s1.charAt(startIndex - 1) != ' ') return false;
			if(endIndex < s1.length() - 1 && s1.charAt(endIndex + 1) != ' ') return false;
			return true;
		}
	}

	public static String lowerTrimCommaSpace(String s1) {
		return s1.toLowerCase().trim().replace(",", "").replace(" ", "");
	}
	
	public static String lowerTrim(String s1) {
		if(s1!=null) return s1.toLowerCase().trim();
		return null;
	}
	
	public static String replacePer_centToPerCent(String str) {
		return str.replace("per cent", "percent");
	}
}
