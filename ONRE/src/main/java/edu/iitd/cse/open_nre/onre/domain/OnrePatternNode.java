/**
 * 
 */
package edu.iitd.cse.open_nre.onre.domain;

import java.util.ArrayList;
import java.util.List;

import edu.iitd.cse.open_nre.onre.OnreGlobals;
import edu.iitd.cse.open_nre.onre.constants.OnreExtractionPartType;
import edu.iitd.cse.open_nre.onre.utils.OnreUtils;
import edu.iitd.cse.open_nre.onre.utils.OnreUtils_string;
import edu.knowitall.tool.parse.graph.DependencyNode;

/**
 * @author harinder
 *
 */
public class OnrePatternNode { 
	public String dependencyLabel;
	
	public String word;
	public String posTag;
	
	public int index;
	public int offset;
	
	public int level;
	
	public List<OnrePatternNode> children;
	public OnrePatternNode parent;
	
	public int visitedCount; //used while creating the pattern while learning the pattern
	public OnreExtractionPartType nodeType; //used while creating the pattern while learning the pattern
	
	public OnrePatternNode(String nodeString, OnrePatternNode parentNode) {
		String split[] = nodeString.split("#");
		this.dependencyLabel = split[0];
		this.word = split[1];
		this.posTag = split[2];
		this.parent = parentNode;

		this.children = new ArrayList<OnrePatternNode>();
	}
	
	public OnrePatternNode(DependencyNode depNode) {
		//System.out.println(depNode);
		this.word = depNode.text();
		this.posTag = depNode.postag();
		this.index = depNode.index();
		this.offset = depNode.offset();
		
		this.children = new ArrayList<OnrePatternNode>();
	}
	
	public boolean matches(OnrePatternNode regexNode) {
		if(!isMatchPosTag(regexNode)) return false;
		if(!isMatchDepLabel(regexNode)) return false;
		if(!isMatchWord(regexNode)) return false; 
		return true;
	}

	private boolean isValid(OnrePatternNode regexNode) {
		if(!(regexNode.word.startsWith("{") && regexNode.word.endsWith("}"))) return true;
		
		return true;
	}
	
	private boolean isMatchWord(OnrePatternNode regexNode) {
		//TODO: null/empty checks for both this & that
		if(this.word==null || this.word.equals("")) return true;
		if(regexNode.word==null || regexNode.word.equals("")) return true;
		
		//TODO: curly check for both this & that - edit: commented for 'this'
		if(regexNode.word.startsWith("{") && regexNode.word.endsWith("}")) return true;
		
		if(OnreUtils_string.isIgnoreCaseMatch(this.word, regexNode.word)) {
			
			if(OnreUtils_string.isIgnoreCaseMatch(this.word, "was") 
					|| OnreUtils_string.isIgnoreCaseMatch(this.word, "were")
					|| OnreUtils_string.isIgnoreCaseMatch(this.word, "had")) {
				OnreGlobals.isSentenceInPastTense = true;
			}
			
			if(OnreUtils_string.isIgnoreCaseMatch(this.word, "is") 
					|| OnreUtils_string.isIgnoreCaseMatch(this.word, "are")
					|| OnreUtils_string.isIgnoreCaseMatch(this.word, "has")
					|| OnreUtils_string.isIgnoreCaseMatch(this.word, "have")) {
				OnreGlobals.isSentenceInPastTense = false;
			}
			
			if(OnreUtils_string.isIgnoreCaseMatch(this.word, "be")) {
				OnreGlobals.isMatchingWithBe = true;
			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean isMatchDepLabel(OnrePatternNode regexNode) {
		//TODO: null/empty checks for both this & that
		if(this.dependencyLabel==null || this.dependencyLabel.equals("")) return true;
		if(regexNode.dependencyLabel==null || regexNode.dependencyLabel.equals("")) return true;
		
		if(OnreUtils_string.isIgnoreCaseMatch(this.dependencyLabel, regexNode.dependencyLabel)) return true;
		
		return false;
	}

	private boolean isMatchPosTag(OnrePatternNode regexNode) {
		//TODO: null/empty checks for both this & that
		if(this.posTag==null || this.posTag.equals("")) return true;
		if(regexNode.posTag==null || regexNode.posTag.equals("")) return true;
		
		if(OnreUtils_string.isIgnoreCaseMatch(this.posTag, regexNode.posTag)) return true;
		
		return false;
	}
	
	public String toString() {
		return "(" + dependencyLabel + "_" + word + "_" + posTag + ")" + children;
	}
}
