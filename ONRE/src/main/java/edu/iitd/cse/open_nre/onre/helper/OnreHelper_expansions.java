package edu.iitd.cse.open_nre.onre.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import edu.iitd.cse.open_nre.onre.OnreGlobals;
import edu.iitd.cse.open_nre.onre.comparators.OnreComparator_PatternNode_Index;
import edu.iitd.cse.open_nre.onre.constants.OnreFilePaths;
import edu.iitd.cse.open_nre.onre.domain.OnreExtraction;
import edu.iitd.cse.open_nre.onre.domain.OnreExtractionPart;
import edu.iitd.cse.open_nre.onre.domain.OnrePatternNode;
import edu.iitd.cse.open_nre.onre.utils.OnreIO;
import edu.iitd.cse.open_nre.onre.utils.OnreUtils;
import edu.iitd.cse.open_nre.onre.utils.OnreUtils_string;
import edu.iitd.cse.open_nre.onre.utils.OnreUtils_tree;

public class OnreHelper_expansions {
	
	public static void expandExtraction(OnreExtraction onreExtraction, OnrePatternNode patternNode_sentence) throws IOException {
		if(onreExtraction.relation != null) expandRelation(onreExtraction, patternNode_sentence);
		else onreExtraction.relation = new OnreExtractionPart();
		
		expandArgument(onreExtraction, patternNode_sentence);
		expandQuantity(onreExtraction, patternNode_sentence); //TODO: IMPORTANT-CHANGE #14: expand on prep if subtree does not have relation/arg
		if(OnreUtils.quantityExists(onreExtraction)) expandQuantity_settingAdditionalInfo(onreExtraction,patternNode_sentence);
		
		fillNegationAndAuxWords(onreExtraction, patternNode_sentence);
	}
	
	private static void fillNegationAndAuxWords(OnreExtraction onreExtraction, OnrePatternNode patternNode_sentence) {
		OnrePatternNode node_parent_relation = OnreUtils_tree.searchParentOfNodeInTreeByIndex(onreExtraction.relation, patternNode_sentence);
		
		if(node_parent_relation == null) return;
		
		for(OnrePatternNode child : node_parent_relation.children) {
			if(child.dependencyLabel.equals("aux") || child.dependencyLabel.equals("auxpass")) {
				OnreGlobals.auxVerb = child.word;
			}
			if(child.dependencyLabel.equals("neg")) {
				OnreGlobals.negatedWord = child.word;
			}
		}
	}
	
	private static void expandArgument(OnreExtraction onreExtraction, OnrePatternNode patternNode_sentence) {
		OnrePatternNode node_argument = OnreUtils_tree.searchNodeInTreeByIndex(onreExtraction.argument, patternNode_sentence);
		
	    Set<OnrePatternNode> expansions = new HashSet<>();
		expansions.add(node_argument);
		
		expandArgumentHelper_basicExpansions(node_argument, expansions);
		expansions = expandArgumentHelper_expandOnPrepSubTree(onreExtraction, expansions); //TODO: IMPORTANT-CHANGE #13: expand on prep if subtree does not have relation/quantity
		expansions = expandArgumentHelper_expand_rcMod_partmod(onreExtraction, expansions); //TODO: IMPORTANT-CHANGE #9: expandArg: completely expand on rcmod & partmod if subtree does not include rel/quantity

		String str = expandHelper_sortExpansions_createStr(expansions);
		
		// If upon expansion, we include already included text - ignore
		if(expandHelper_isAlreadyPresent(onreExtraction, str, 0)) {
			String newStr = "";
			String []words = str.split(" ");
			for(String word : words) {
				if(onreExtraction.quantity!=null && OnreUtils_string.isIgnoreCaseIgnoreCommaIgnoreSpaceContains(onreExtraction.quantity.text, word)) continue;
				if(onreExtraction.relation!=null && OnreUtils_string.isIgnoreCaseIgnoreCommaIgnoreSpaceContains(onreExtraction.relation.text, word)) continue;
				newStr = newStr + word + " ";
			}
			if(newStr.trim().length() != 0) onreExtraction.argument.text = newStr.trim();
			return;		
		}
		onreExtraction.argument.text = str;
    }

	private static void expandArgumentHelper_basicExpansions(OnrePatternNode node_argument, Set<OnrePatternNode> expansions) {
		Queue<OnrePatternNode> q_yetToExpand = new LinkedList<OnrePatternNode>();
		q_yetToExpand.add(node_argument);
		while(!q_yetToExpand.isEmpty()) {
			OnrePatternNode currNode = q_yetToExpand.remove();
			
			for(OnrePatternNode child : currNode.children) {
				if(child.dependencyLabel.equals("poss")) { expansions.add(child); q_yetToExpand.add(child); }
				if(child.dependencyLabel.equals("possessive")) { expansions.add(child); q_yetToExpand.add(child); }
				
				if(child.dependencyLabel.equals("det")) { expansions.add(child); q_yetToExpand.add(child); }
				if(child.dependencyLabel.equals("num")) { expansions.add(child); q_yetToExpand.add(child); }
				if(child.dependencyLabel.equals("neg")) { expansions.add(child); q_yetToExpand.add(child); }
				
				if(child.dependencyLabel.matches(".*mod") && !child.dependencyLabel.equals("rcmod") && !child.dependencyLabel.equals("partmod")) { expansions.add(child); q_yetToExpand.add(child); }
				
				if(child.dependencyLabel.equals("nn")) { expansions.add(child); q_yetToExpand.add(child); }
				
				if(child.dependencyLabel.equals("pobj")) { expansions.add(child); q_yetToExpand.add(child); }
				
				if(child.dependencyLabel.equals("cc") || child.dependencyLabel.equals("conj")) { expansions.add(child); q_yetToExpand.add(child); }
			}
		}
	}
	
	private static Set<OnrePatternNode> expandArgumentHelper_expandOnPrepSubTree(OnreExtraction onreExtraction, Set<OnrePatternNode> expansions) {
		Set<OnrePatternNode> expansions_all = new HashSet<>();
	    expansions_all.addAll(expansions);
	    for (OnrePatternNode onrePatternNode : expansions) {
	    	for(OnrePatternNode child : onrePatternNode.children) {
	    		OnrePatternNode node_prep = null;
	    		if(child.dependencyLabel.equals("prep")) node_prep = child;
	    		
	    		if(node_prep == null) continue;
	    		if(OnreUtils_tree.searchNodeInTreeByIndex(onreExtraction.quantity, child) != null) continue;
	    		if(OnreUtils_tree.searchNodeInTreeByIndex(onreExtraction.relation_headWord, child) != null) continue;
	    		
	    		expansions_all.addAll(expandHelper_expandCompleteSubTree(node_prep));
	    	}
		}
	    expansions = expansions_all;
		return expansions;
	}
	
	private static Set<OnrePatternNode> expandArgumentHelper_expand_rcMod_partmod(OnreExtraction onreExtraction, Set<OnrePatternNode> expansions) {
		Set<OnrePatternNode> expansions_all = new HashSet<>();
	    expansions_all.addAll(expansions);
	    
	    for (OnrePatternNode onrePatternNode : expansions) {
	    	for(OnrePatternNode child : onrePatternNode.children) {
	    		if(!child.dependencyLabel.equals("rcmod") && !child.dependencyLabel.equals("partmod")) continue;
		    	
		    	if(OnreUtils_tree.searchNodeInTreeByIndex(onreExtraction.quantity, child) != null) continue;
	    		if(OnreUtils_tree.searchNodeInTreeByIndex(onreExtraction.argument_headWord, child) != null) continue;
	    		
	    		expansions_all.addAll(expandHelper_expandCompleteSubTree(child));
	    	}
	    	
	    }
	    	
	    expansions = expansions_all;
		return expansions;
	}
	
	private static void expandQuantity(OnreExtraction onreExtraction, OnrePatternNode patternNode_sentence) {
		if(onreExtraction.quantity==null) return;

		OnrePatternNode quantity = OnreUtils_tree.searchNodeInTreeByIndex(onreExtraction.quantity, patternNode_sentence);
		
		OnrePatternNode node_prep = null;
		for(OnrePatternNode child : quantity.children) {
			if(child.dependencyLabel.equals("prep")) node_prep = child;
		}
		
		if(subtreeContainsOver(quantity) && !OnreUtils_string.isIgnoreCaseContainsPhrase(onreExtraction.quantity.text, "over")) {
			onreExtraction.quantity.text = "over " + onreExtraction.quantity.text;
		}
		
		if(node_prep == null) return;
		
	    Set<OnrePatternNode> expansions = expandHelper_expandCompleteSubTree(node_prep);
	    
		String quantity_unit_plus = expandHelper_sortExpansions_createStr(expansions);
		// If upon expansion, we include already included text - ignore
		if(expandHelper_isAlreadyPresent(onreExtraction, quantity_unit_plus, 2)) return;
		
		String qPhraseExceptValue = OnreHelper_DanrothQuantifier.getPhraseExceptValue(onreExtraction.quantity.text);
		
		quantity_unit_plus = quantity_unit_plus.replaceAll("^"+qPhraseExceptValue, "");
		quantity_unit_plus = quantity_unit_plus.replaceAll("^"+onreExtraction.q_unit, "");
		
		if(node_prep.word.equals("of"))
			onreExtraction.quantity_unit_plus = new OnreExtractionPart(quantity_unit_plus, node_prep.index);
		else
			onreExtraction.additional_info = new OnreExtractionPart(quantity_unit_plus, node_prep.index);
    }
	
	private static boolean subtreeContainsOver(OnrePatternNode quantityNode) {
		
		Queue<OnrePatternNode> q_yetToExpand = new LinkedList<OnrePatternNode>();
		q_yetToExpand.add(quantityNode);
		while(!q_yetToExpand.isEmpty()) {
			OnrePatternNode currNode = q_yetToExpand.remove();
			
			for(OnrePatternNode child : currNode.children) {
				if(child.word.equalsIgnoreCase("over")) return true;
				q_yetToExpand.add(child);
			}
		}
		
		return false;
	}

	private static void expandQuantity_settingAdditionalInfo(OnreExtraction onreExtraction, OnrePatternNode patternNode_sentence) {
		
		OnrePatternNode quantity_parent_node = OnreUtils_tree.searchParentOfNodeInTreeByIndex(onreExtraction.quantity, patternNode_sentence);
		if(quantity_parent_node==null) return;
		
		for(OnrePatternNode child : quantity_parent_node.children) {
			OnrePatternNode node_prep = null;
			if(child.dependencyLabel.equals("prep") || child.dependencyLabel.equals("advcl") || child.word.equalsIgnoreCase("prior")) node_prep = child;
			if(node_prep == null) continue;
			
			Set<OnrePatternNode> expansions = expandHelper_expandCompleteSubTree(node_prep);
			
			String additional_info = expandHelper_sortExpansions_createStr(expansions);
			if(expandHelper_isAlreadyPresent(onreExtraction, additional_info, 2)) return;			// If upon expansion, we include already included text - ignore
			if(onreExtraction.additional_info == null) {
				onreExtraction.additional_info = new OnreExtractionPart(additional_info, node_prep.index);
			}
			else {
				onreExtraction.additional_info.text = onreExtraction.additional_info.text + " ; " + additional_info;
			}
		}
	}
	
	private static void expandRelation(OnreExtraction onreExtraction, OnrePatternNode patternNode_sentence) throws IOException {
		OnrePatternNode node_relation = OnreUtils_tree.searchNodeInTreeByIndex(onreExtraction.relation, patternNode_sentence);
		
	    Set<OnrePatternNode> expansions = new HashSet<>();
	    expansions.add(node_relation);

	    expandRelationHelper_basicExpansions(node_relation, expansions);
	    expandRelationHelper_expandOnPrepForConfiguredWords(onreExtraction, node_relation, expansions); //TODO: IMPORTANT-CHANGE #6: if the relation word is one of configured words(grew/increased/down), then expand it on prep
	    expansions = expandRelationHelper_expandOnPrepSubTree(onreExtraction, expansions); //TODO: IMPORTANT-CHANGE #12: expand on prep (except headWord) if subtree does not have argument/quantity
	    expansions = expandRelationHelper_expandOnAuxVerbs(onreExtraction, expansions);
	    
		String str = expandHelper_sortExpansions_createStr(expansions);
		if(expandHelper_isAlreadyPresent(onreExtraction, str, 1)) return;		// If upon expansion, we include already included text - ignore
		onreExtraction.relation.text = str;
    }

	private static void expandRelationHelper_basicExpansions(OnrePatternNode node_relation, Set<OnrePatternNode> expansions) {
		Queue<OnrePatternNode> q_yetToExpand = new LinkedList<OnrePatternNode>();
	    q_yetToExpand.add(node_relation);
	    while(!q_yetToExpand.isEmpty())
	    {
	    	OnrePatternNode currNode = q_yetToExpand.remove();
	    	for(OnrePatternNode child : currNode.children) {
				if(child.dependencyLabel.equals("nn")) {expansions.add(child); q_yetToExpand.add(child); } 
				if(child.dependencyLabel.equals("neg")) {expansions.add(child); q_yetToExpand.add(child); }//TODO: IMPORTANT-CHANGE #10: negation handling
				
				if(child.dependencyLabel.matches(".*mod") && !child.dependencyLabel.equals("npadvmod") && !child.dependencyLabel.equals("advmod")) { expansions.add(child); q_yetToExpand.add(child); }
				
				if(child.dependencyLabel.matches("det")) { expansions.add(child); q_yetToExpand.add(child); }
				
				if(child.word.equalsIgnoreCase("each")) {expansions.add(child);}
			}
	    }
	}

	private static void expandRelationHelper_expandOnPrepForConfiguredWords(OnreExtraction onreExtraction, OnrePatternNode node_relation, Set<OnrePatternNode> expansions) throws IOException {
		
	    List<String> expandOnPrep = OnreIO.readFile_classPath(OnreFilePaths.filePath_expandOnPrep);
		for(OnrePatternNode child : node_relation.children) {
			if(!child.dependencyLabel.equals("prep")) continue;
			if(OnreUtils_tree.searchNodeInTreeByIndex(onreExtraction.quantity, child) == null) continue;
			OnreGlobals.expandedOnPrep = child.word;
		}
	}
	
	private static Set<OnrePatternNode> expandRelationHelper_expandOnPrepSubTree(OnreExtraction onreExtraction, Set<OnrePatternNode> expansions) {
		Set<OnrePatternNode> expansions_all = new HashSet<>();
	    expansions_all.addAll(expansions);
	    for (OnrePatternNode onrePatternNode : expansions) {
	    	if(onrePatternNode.word.equals(onreExtraction.relation_headWord.text)) continue; //not expanding on headWord?
	    	for(OnrePatternNode child : onrePatternNode.children) {
	    		if(!child.dependencyLabel.equals("prep")) continue;
	    		
	    		if(OnreUtils_tree.searchNodeInTreeByIndex(onreExtraction.quantity, child) != null) continue;
	    		if(OnreUtils_tree.searchNodeInTreeByIndex(onreExtraction.argument_headWord, child) != null) continue;
	    		
	    		expansions_all.addAll(expandHelper_expandCompleteSubTree(child));
	    	}
		}
	    expansions = expansions_all;
		return expansions;
	}
	
	private static Set<OnrePatternNode> expandRelationHelper_expandOnAuxVerbs(OnreExtraction onreExtraction, Set<OnrePatternNode> expansions) {
		Set<OnrePatternNode> expansions_all = new HashSet<>();
	    expansions_all.addAll(expansions);
	    for (OnrePatternNode onrePatternNode : expansions) {
	    	if(onrePatternNode.posTag.equals("VERB")) {
	    		for(OnrePatternNode child : onrePatternNode.children) {
		    		if(child.dependencyLabel.equals("aux") || child.dependencyLabel.equals("auxpass")) expansions_all.add(child);
		    	}
	    	}
		}
	    expansions = expansions_all;
		return expansions;
	}

	private static boolean expandHelper_isAlreadyPresent(OnreExtraction onreExtraction,	String str, int type) {
		//here we are checking the complete extractionPart rather than just their headWord, let it be like this only
		switch(type) {
		case 0: // expanding argument
			if(onreExtraction.quantity!=null && OnreUtils_string.isIgnoreCaseIgnoreCommaIgnoreSpaceContains(str, onreExtraction.quantity.text)) return true;
			if(onreExtraction.relation!=null && OnreUtils_string.isIgnoreCaseIgnoreCommaIgnoreSpaceContains(str, onreExtraction.relation.text)) return true;
			break;
		case 1: // expanding relation
			if(onreExtraction.argument!=null && OnreUtils_string.isIgnoreCaseIgnoreCommaIgnoreSpaceContains(str, onreExtraction.argument.text)) return true;
			if(onreExtraction.quantity!=null && OnreUtils_string.isIgnoreCaseIgnoreCommaIgnoreSpaceContains(str, onreExtraction.quantity.text)) return true;
			break;
		case 2: // expanding quantity_percent or setting additional_info
			if(onreExtraction.argument!=null && OnreUtils_string.isIgnoreCaseIgnoreCommaIgnoreSpaceContains(str, onreExtraction.argument.text)) return true;
			if(onreExtraction.relation!=null && OnreUtils_string.isIgnoreCaseIgnoreCommaIgnoreSpaceContains(str, onreExtraction.relation.text)) return true;
			if(onreExtraction.quantity!=null && OnreUtils_string.isIgnoreCaseIgnoreCommaIgnoreSpaceContains(str, onreExtraction.quantity.text)) return true;
			
			if(onreExtraction.quantity!=null && OnreUtils_string.isIgnoreCaseIgnoreCommaIgnoreSpaceContains(onreExtraction.quantity.text, str)) return true;
			//if(onreExtraction.quantity!=null && OnreUtils_string.isIgnoreCaseContainsPhrase(onreExtraction.quantity.text, str)) return true;
			break;
		}
		
		return false;
	}
	
	private static Set<OnrePatternNode> expandHelper_expandCompleteSubTree(OnrePatternNode nodeToExpand) {
		Set<OnrePatternNode> expansions = new HashSet<>();
	    expansions.add(nodeToExpand);
		
		expandHelper_expandCompleteSubTree(nodeToExpand, expansions);
		return expansions;
	}

	private static void expandHelper_expandCompleteSubTree(OnrePatternNode nodeToExpand, Set<OnrePatternNode> expansions) {
		Queue<OnrePatternNode> q_yetToExpand = new LinkedList<OnrePatternNode>();
		q_yetToExpand.add(nodeToExpand);
		while(!q_yetToExpand.isEmpty()) {
			OnrePatternNode currNode = q_yetToExpand.remove();
			
			for(OnrePatternNode child : currNode.children) {
				expansions.add(child); q_yetToExpand.add(child);
			}
		}
	}

	private static String expandHelper_sortExpansions_createStr(Set<OnrePatternNode> expansions_set) {
		
		List<OnrePatternNode> expansions_list = new ArrayList<>(expansions_set);
		Collections.sort(expansions_list, new OnreComparator_PatternNode_Index());
		StringBuilder sb = new StringBuilder("");
		for (OnrePatternNode expansion : expansions_list) {
			sb.append(expansion.word + " ");
        }
		return sb.toString().trim();
	}
}
