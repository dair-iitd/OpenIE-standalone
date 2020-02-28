package edu.iitd.cse.open_nre.onre.utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import edu.iitd.cse.open_nre.onre.comparators.OnreComparator_PatternNode_depLabel;
import edu.iitd.cse.open_nre.onre.domain.OnreExtractionPart;
import edu.iitd.cse.open_nre.onre.domain.OnrePatternNode;

public class OnreUtils_tree {

	public static void sortPatternTree(OnrePatternNode onrePatternNode) {
		Queue<OnrePatternNode> q_patternNode = new LinkedList<>();
		q_patternNode.add(onrePatternNode);
		
		while(!q_patternNode.isEmpty()) {
			OnrePatternNode currNode = q_patternNode.remove();
			Collections.sort(currNode.children, new OnreComparator_PatternNode_depLabel());
			q_patternNode.addAll(currNode.children);
		}
	}
	
	public static OnrePatternNode searchNodeInTreeByText(String text, OnrePatternNode tree) {
		Queue<OnrePatternNode> q_patternNode = new LinkedList<>();
		q_patternNode.add(tree);
		
		while(!q_patternNode.isEmpty()) {
			OnrePatternNode currNode = q_patternNode.remove();
			if(currNode.word.equalsIgnoreCase(text)) return currNode;
			q_patternNode.addAll(currNode.children);
		}
		
		return null;
	}
	
	public static OnrePatternNode searchNodeInTreeByIndex(OnreExtractionPart onreExtractionPart, OnrePatternNode tree) {
		if(onreExtractionPart==null) return null;
		
		Queue<OnrePatternNode> q_patternNode = new LinkedList<>();
		q_patternNode.add(tree);
		
		while(!q_patternNode.isEmpty()) {
			OnrePatternNode currNode = q_patternNode.remove();
			if(currNode.index==onreExtractionPart.index) return currNode;
			q_patternNode.addAll(currNode.children);
		}
		
		return null;
	}
	
	public static OnrePatternNode searchParentOfNodeInTreeByIndex(OnreExtractionPart onreExtractionPart, OnrePatternNode tree) {
		if(onreExtractionPart==null) return null;
		
		Queue<OnrePatternNode> q_patternNode = new LinkedList<>();
		q_patternNode.add(tree);
		
		while(!q_patternNode.isEmpty()) {
			OnrePatternNode currNode = q_patternNode.remove();
			for(OnrePatternNode child : currNode.children) if(child.index == onreExtractionPart.index) return currNode;
			q_patternNode.addAll(currNode.children);
		}
		
		return null;
	}
}
