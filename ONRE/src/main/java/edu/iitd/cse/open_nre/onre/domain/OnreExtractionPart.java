/**
 * 
 */
package edu.iitd.cse.open_nre.onre.domain;

import edu.knowitall.collection.immutable.Interval;

/**
 * @author harinder
 *
 */
public class OnreExtractionPart {
	public String text;
	public Interval offsetInterval;
	public int index; //required for expanding extraction
	
	public String posTag;
	
	public OnreExtractionPart() {
		this.text = "";
		this.offsetInterval = new Interval(0, 0); //TODO: dummy offset
	}
	
	public OnreExtractionPart(String text) {
		this.text = text.trim();
		this.offsetInterval = new Interval(0, 0); //TODO: dummy offset
	}
	
	public OnreExtractionPart(String text, int index) {
		this.text = text.trim();
		this.index = index;
		this.offsetInterval = new Interval(0, 0); //TODO: dummy offset
	}
	
	public OnreExtractionPart(String text, int index, String posTag) {
		this.text = text.trim();
		this.index = index;
		this.posTag = posTag;
		this.offsetInterval = new Interval(0, 0); //TODO: dummy offset
	}
	
	@Override
	public String toString() {
		return this.text;
	}
}
