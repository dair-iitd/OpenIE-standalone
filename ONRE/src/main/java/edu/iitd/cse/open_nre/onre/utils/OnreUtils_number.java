package edu.iitd.cse.open_nre.onre.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Pattern;

public class OnreUtils_number {

	public static boolean isNumber(String str) {
		Pattern numberPat = Pattern.compile("^[\\+-]?\\d+([,\\.]\\d+)*([eE]-?\\d+)?$");
		return numberPat.matcher(str.toString()).find();
	}
	
	public static Double str2Double(String str) throws ParseException {
		NumberFormat format = NumberFormat.getInstance(Locale.US);
        Number number = format.parse(str);
        return number.doubleValue();
	}
}
