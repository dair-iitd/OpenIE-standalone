/**
 * 
 */
package edu.iitd.cse.open_nre.onre.constants;

import java.util.HashMap;
import java.util.Map;

public enum Onre_dsRunType {
	TYPE1("type1"),//no value comparison, ignore if multiwords unit or if no unit
	TYPE2("type2"),//value comparison as a string, ignore if multiwords unit or if no unit
	TYPE3("type3"),//value comparison as a number, ignore if multiwords unit or if no unit
	TYPE4("type4"),//value comparison as a number, ignore if multiwords unit, no unit allowed
	TYPE5("type5"); //value comparison as a number, multiwords unit allowed, no unit allowed
	
	public String text;
	private static Map<String, Onre_dsRunType> typeMap;
	
	public static Onre_dsRunType DEFAULT = Onre_dsRunType.TYPE5;
	
	private Onre_dsRunType(String text) {
	  this.text = text;
    }
	
	public static Onre_dsRunType getType(String text) {
		if(typeMap==null) populateTypeMap();
		
		Onre_dsRunType type = typeMap.get(text);
		if(type==null) return DEFAULT;
		return type;
	}

	private static void populateTypeMap() {
	    typeMap = new HashMap<>();
	    for (Onre_dsRunType runType : Onre_dsRunType.values()) {
	        typeMap.put(runType.text, runType);
	    }
    }
}
