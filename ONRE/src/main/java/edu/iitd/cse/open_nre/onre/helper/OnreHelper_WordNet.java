/**
 * 
 */
package edu.iitd.cse.open_nre.onre.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.iitd.cse.open_nre.onre.constants.OnreFilePaths;
import edu.iitd.cse.open_nre.onre.utils.OnreIO;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.WordnetStemmer;

/**
 * @author swarna
 *
 */
public class OnreHelper_WordNet {
	
	private static IDictionary getWordnetDictionary() throws IOException {
		//construct URL to WordNet Dictionary directory on the computer
        String wordNetDirectory = OnreFilePaths.folderpath_wordnet;
        String path = wordNetDirectory + File.separator + "dict";
        URL url = new URL("file", null, path);      

        //construct the Dictionary object and open it
        IDictionary dict = new Dictionary(url);
        return dict;
	}
	
	public static String getStemmedWord(String word) throws IOException {
		IDictionary dict = getWordnetDictionary();
		dict.open();
		
		WordnetStemmer stemmer = new WordnetStemmer(dict);
		List<String> stemmedWords = stemmer.findStems(word, POS.VERB);
		
		for (int i = 0; i < stemmedWords.size(); i++) {
            if(!stemmedWords.equals(word)) {
            	dict.close();
            	return stemmedWords.get(i);
            }
        }
		
		dict.close();
		return null;
	}
	
	public static String getDerivationallyRelatedNounWord(String word, int partOfSpeech) throws IOException {
		IIndexWord idxWord;
		IDictionary dict = getWordnetDictionary();
		dict.open();
		
		if(partOfSpeech == 0) { // Verb
			idxWord = dict.getIndexWord (word, POS.VERB );
		}
		else if(partOfSpeech == 1) { // Adjective
			idxWord = dict.getIndexWord (word, POS.ADJECTIVE );
		}
		else { // Adverb
			idxWord = dict.getIndexWord (word, POS.ADVERB );
		}
		
		if(idxWord == null || idxWord.getWordIDs().isEmpty()) {
			dict.close();
			return null;
		}
		IWordID wordID = idxWord.getWordIDs().get(0) ;
        IWord Iword = dict.getWord (wordID);
        
        for(int i=0;i<Iword.getRelatedWords(Pointer.DERIVATIONALLY_RELATED).size();i++) {
	        IWord derivedWord = dict.getWord(Iword.getRelatedWords(Pointer.DERIVATIONALLY_RELATED).get(i));
	        String pos = derivedWord.getPOS().toString();
	        if(pos.equals("noun") && !derivedWord.getLemma().equals(word)) 
	        {
	        	dict.close();
	        	return derivedWord.getLemma();
	        }
        }
        
        dict.close();
        return null;
	}
	
	public static String getWhoseAttributeIsWord(String word) throws IOException {
		List<String> inputJsonStrings = OnreIO.readFile_classPath(OnreFilePaths.filepath_wordnetAttributes);
        Type mapType = new TypeToken<Map<String, Set<String>>>(){}.getType();  
        Map<String, Set<String>> attributeMap = new Gson().fromJson(inputJsonStrings.get(0), mapType);
        
        for(String key : attributeMap.keySet()) {
        	Set<String> value = attributeMap.get(key);
        	if(value.contains(word)) return key;
        }
        
        return null;
	}
	
	
}
