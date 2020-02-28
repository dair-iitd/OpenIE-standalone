/**
 * 
 */
package edu.iitd.cse.open_nre.onre.domain;


/**
 * @author harinder
 *
 */
public class OnrePatternTree { 
	public String sentence;
	public OnrePatternNode root;
	
	public OnrePatternTree(String sentence, OnrePatternNode root) {
		this.sentence = sentence;
		this.root = root;
	}
}
