/**
 * 
 */
package edu.iitd.cse.open_nre.onre.domain;

import edu.iitd.cse.open_nre.onre.OnreGlobals;
import edu.iitd.cse.open_nre.onre.constants.OnreConstants;

/**
 * @author harinder
 *
 */
public class OnreExtraction {
	
	public String sentence;
	public Integer	patternNumber;
	
	public OnreExtractionPart	argument;
	public OnreExtractionPart	relation;
	public OnreExtractionPart	quantity;
	public OnreExtractionPart	additional_info;
	
	public OnreExtractionPart	quantity_unit_plus; 
	
	public OnreExtractionPart	quantity_percent;
	
	public OnreExtractionPart	argument_headWord;
	public OnreExtractionPart	relation_headWord;
	public String q_unit;
	public Double q_value;
	public String argHeadWord_PosTag;


	public OnreExtraction() {
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(OnreGlobals.arg_onre_isSeedFact) {
			//sb.append("\n").append(this.sentence).append("\n"); //TODO
			//sb.append(this.patternNumber).append("\n"); //TODO
			sb.append("(");
			sb.append(this.argument_headWord);
			sb.append("(" + argHeadWord_PosTag + ")");
			sb.append(OnreConstants.DELIMETER_EXTR);
			sb.append(this.relation_headWord);
			sb.append(OnreConstants.DELIMETER_EXTR);
			sb.append(this.q_value);
			sb.append(OnreConstants.DELIMETER_EXTR);
			sb.append(this.q_unit);
			sb.append(OnreConstants.DELIMETER_EXTR);
			sb.append(this.relation);
			sb.append(")");
		}
		else {
			sb.append("(");
			sb.append(this.argument);
			sb.append(OnreConstants.DELIMETER_EXTR);
			sb.append(this.relation);
			sb.append(OnreConstants.DELIMETER_EXTR);
			sb.append(this.quantity);
			if(this.quantity_unit_plus!=null) sb.append(" ").append(this.quantity_unit_plus);
			if(this.additional_info!=null && this.additional_info.text!=null && !this.additional_info.text.isEmpty()) {
				sb.append(OnreConstants.DELIMETER_EXTR);
				sb.append(this.additional_info);
			}
			sb.append(")");
		}
		
		return sb.toString();
	}
}
