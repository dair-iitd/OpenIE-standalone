/**
 * 
 */
package edu.iitd.cse.open_nre.onre.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import edu.iitd.cse.open_nre.onre.OnreGlobals;
import edu.iitd.cse.open_nre.onre.constants.OnreFilePaths;
import edu.iitd.cse.open_nre.onre.domain.OnrePatternNode;
import edu.iitd.cse.open_nre.onre.domain.OnrePatternTree;
import edu.iitd.cse.open_nre.onre.domain.Onre_dsDanrothSpan;
import edu.iitd.cse.open_nre.onre.utils.OnreIO;
import edu.iitd.cse.open_nre.onre.utils.OnreUtils_number;
import edu.iitd.cse.open_nre.onre.utils.OnreUtils_tree;

/**
 * @author harinder
 *
 */
public class OnreHelper_pattern {
	
	public static List<OnrePatternNode> getDepPatterns() throws IOException {
		List<OnrePatternNode> list_depPattern = new ArrayList<OnrePatternNode>();
		
		list_depPattern.addAll(getSeedPatterns());
		if(!OnreGlobals.arg_onre_isSeedFact) list_depPattern.addAll(getConfiguredPatterns());
		
		return list_depPattern;
	}
	
	private static List<OnrePatternNode> getSeedPatterns() throws IOException {
		List<OnrePatternNode> list_seedPattern = new ArrayList<OnrePatternNode>();
		List<String> configuredPatterns = OnreIO.readFile_classPath(OnreFilePaths.filePath_seedPatterns);

		for (String configuredPattern : configuredPatterns) {
			if(configuredPattern.trim().length()==0) {list_seedPattern.add(null); continue;}

			list_seedPattern.add(convertPattern2PatternTree(configuredPattern));
        }
		
		return list_seedPattern;
	}
	
	private static List<OnrePatternNode> getConfiguredPatterns() throws IOException {
		List<OnrePatternNode> list_configuredPattern = new ArrayList<OnrePatternNode>();
		List<String> configuredPatterns = OnreIO.readFile_classPath(OnreFilePaths.filePath_depPatterns);

		for (String configuredPattern : configuredPatterns) {
			if(configuredPattern.trim().length()==0) {list_configuredPattern.add(null); continue;}

			if(configuredPattern.contains("(nn#")) {list_configuredPattern.add(null); continue;} //TODO: IMPORTANT-CHANGE #1:ignoring patterns with depLabel as nn (nn#)
			if(configuredPattern.contains("{arg}#dt")) {list_configuredPattern.add(null); continue;} //TODO: IMPORTANT-CHANGE #2:ignoring patterns with {arg} postag as dt
			if(configuredPattern.contains("rel}#in)")) {list_configuredPattern.add(null); continue;} //TODO: IMPORTANT-CHANGE #3:ignoring patterns with {rel} postag as IN
			
			list_configuredPattern.add(convertPattern2PatternTree(configuredPattern));
        }
		
		return list_configuredPattern;
	}
	
	public static OnrePatternNode convertPattern2PatternTree(String pattern) {
		OnrePatternNode onrePatternNode = convertPattern2PatternTree_helper(pattern);
		OnreUtils_tree.sortPatternTree(onrePatternNode);
		return onrePatternNode;
	}
	
	private static OnrePatternNode convertPattern2PatternTree_helper(String pattern) {
		Stack<Character> myStack = new Stack<>();
		myStack.push(pattern.charAt(0));
		int index=1;
		
		OnrePatternNode onrePatternNode = null;
		while(!myStack.isEmpty()) {
			while(pattern.charAt(index)=='(' || pattern.charAt(index)=='<') {
				StringBuilder sb = new StringBuilder("");
				index = setNodeString(pattern, index, sb);
				OnrePatternNode onrePatternNode_child = new OnrePatternNode(sb.toString(), onrePatternNode);
				if(onrePatternNode==null) onrePatternNode=onrePatternNode_child; //at the start
				else onrePatternNode.children.add(onrePatternNode_child);
				
				if(pattern.charAt(index)=='<') {
					myStack.push('<');
					onrePatternNode = onrePatternNode_child;
				}
			}
			
			while(pattern.charAt(index)=='>') { 
				myStack.pop(); 
				if(onrePatternNode.parent==null) return onrePatternNode; 
				onrePatternNode = onrePatternNode.parent; 
				index++; 
			}
		}
		
		return onrePatternNode;
	}
	
    public static int setNodeString(String pattern, int index, StringBuilder sb) {
	    if(pattern.charAt(index)=='<') index++;
		index++; //current index: '('...moving to next index
		
		while(pattern.charAt(index) != ')') {
			sb.append(pattern.charAt(index));
			index++;
		}
		
		index++; //current index: ')'...moving to next index
	    return index;
    }

//------------ONRE_DS helper functions--------------------
    public static OnrePatternNode searchNode(OnrePatternTree onrePatternTree, String word, Onre_dsDanrothSpan danrothSpan) {
		OnrePatternNode root = onrePatternTree.root;
		
		Queue<OnrePatternNode> myQ = new LinkedList<>();
		myQ.add(root);
		myQ.add(null);
		
		int level = 0;
		while(!myQ.isEmpty()) {
			OnrePatternNode currNode = myQ.remove();
			if(currNode==null && myQ.isEmpty()) break; 
			if(currNode==null) {level++; myQ.add(null); continue;}
			
			if(nodeFound(word, currNode, danrothSpan)) {currNode.level=level; return currNode;}
			
			List<OnrePatternNode> children = currNode.children;
			for (OnrePatternNode child : children) {
				myQ.add(child);
			}
		}
		
		return null;
	}
    
    public static OnrePatternNode searchNode(OnrePatternTree onrePatternTree, String word) {
		OnrePatternNode root = onrePatternTree.root;
		
		Queue<OnrePatternNode> myQ = new LinkedList<>();
		myQ.add(root);
		
		while(!myQ.isEmpty()) {
			OnrePatternNode currNode = myQ.remove();
			
			if(nodeFound(word, currNode)) return currNode;
			
			List<OnrePatternNode> children = currNode.children;
			for (OnrePatternNode child : children) {
				myQ.add(child);
			}
		}
		
		return null;
	}
	
    private static boolean nodeFound(String word, OnrePatternNode currNode, Onre_dsDanrothSpan danrothSpan) {
		if(currNode.word.equalsIgnoreCase(word) && currNode.offset>=danrothSpan.start && currNode.offset<=danrothSpan.end) return true;
		return false;
	}
    
	private static boolean nodeFound(String word, OnrePatternNode currNode) {
		
		if(currNode.word.equalsIgnoreCase(word)) return true;
		
		//compare as a number
		try {
			if(OnreUtils_number.str2Double(currNode.word).equals(OnreUtils_number.str2Double(word))) return true;
		}catch(Exception e){
			//ignoring the exception--prob bcauz string can't be converted to a number
		}
		
		return false;
	}
	
	public static void markUnvisited(OnrePatternNode node) {
		OnrePatternNode temp = node;
		while(temp!=null) {
			temp.visitedCount--;
			temp = temp.parent;
		}
	}
	
	public static void markVisited(OnrePatternNode node) {
		OnrePatternNode temp = node;
		while(temp!=null) {
			temp.visitedCount++;
			temp = temp.parent;
		}
	}
	
	public static int getDistanceBetweenNodes(OnrePatternNode higherLevelNode, OnrePatternNode lowerLevelNode) {
		int distance = 0;
		
		OnrePatternNode temp = lowerLevelNode;
		while(temp != higherLevelNode) {
			distance++; 
			if(temp==null) return Integer.MAX_VALUE; 
			temp=temp.parent;
		}
		
		return distance;
	}
	
	public static OnrePatternNode getIntersectionNode(List<OnrePatternNode> ancestors_qValue, List<OnrePatternNode> ancestors_qUnit) {
		int cntr = 0; 
		OnrePatternNode ancestor_qValue_temp = ancestors_qValue.get(cntr);
		OnrePatternNode ancestor_qUnit_temp = ancestors_qUnit.get(cntr);
		
		if(ancestor_qUnit_temp!=ancestor_qValue_temp) return null;
		
		++cntr;
		
		OnrePatternNode currentIntersectionNode = null;
		while(ancestor_qUnit_temp == ancestor_qValue_temp) {
			currentIntersectionNode = ancestor_qUnit_temp;
			if(cntr>=ancestors_qValue.size() || cntr>=ancestors_qUnit.size()) break;
			ancestor_qValue_temp = ancestors_qValue.get(cntr);
			ancestor_qUnit_temp = ancestors_qUnit.get(cntr);
			++cntr;
		}
		
		return currentIntersectionNode;
	}
	
	public static List<OnrePatternNode> getAncestors(OnrePatternNode node) {
		List<OnrePatternNode> ancestors = new ArrayList<>();
		OnrePatternNode temp = node;
		while(temp !=null) {
			ancestors.add(temp);
			temp = temp.parent;
		}
		
		Collections.reverse(ancestors);
		return ancestors;
	}
	
	public static OnrePatternNode searchNode_markVisited(OnrePatternTree onrePatternTree, String word) {
		OnrePatternNode node = searchNode(onrePatternTree, word);
		if(node==null) return null;
		
		markVisited(node);
		return node;
	}
	
	//lowest node with visited count 3(visited by all three factWords)
	public static OnrePatternNode findLCA(OnrePatternTree onrePatternTree) {
		OnrePatternNode root = onrePatternTree.root;
		
		OnrePatternNode LCA = null;
		
		Queue<OnrePatternNode> myQ = new LinkedList<>();
		myQ.add(root);
		
		while(!myQ.isEmpty()) {
			OnrePatternNode currNode = myQ.remove();
			if(currNode.visitedCount == 3) LCA = currNode;
			
			List<OnrePatternNode> children = currNode.children;
			for (OnrePatternNode child : children) {
				myQ.add(child);
			}
		}
		
		return LCA;
	}
}
