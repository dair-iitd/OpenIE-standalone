/**
 * 
 */
package edu.iitd.cse.open_nre.onre.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author harinder
 *
 */
public class OnreIO {
	
	public static List<String> readFile(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		
		List<String> lines = new ArrayList<>();
		
		String line = br.readLine();
		while(line != null) {
			if(!line.trim().isEmpty()) lines.add(line); 
			line = br.readLine();
		}
		
		br.close();
		return lines;
	}
	
	public static List<String> readFile_classPath(String filePath) throws IOException {
		InputStream in = OnreIO.class.getResourceAsStream(filePath);
		
		if(in == null) {
			System.err.println("ERROR :: ---Not able to read " + filePath + "...exiting---");
			System.exit(1);
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		List<String> lines = new ArrayList<>();
		
		String line = br.readLine();
		while(line != null) {
			if(!line.trim().isEmpty()) lines.add(line); 
			line = br.readLine();
		}
		
		br.close();
		return lines;
	}
	
	public static void writeFile(String filePath, @SuppressWarnings("rawtypes") List lines) throws IOException {
		PrintWriter pw = new PrintWriter(filePath);
		for (Object object : lines) pw.println(object.toString());
		pw.close();
	}
	
	public static void writeMap(String filePath, Map<String, Integer> lines) throws IOException {
		PrintWriter pw = new PrintWriter(filePath);
		for (Map.Entry<String, Integer> entry : lines.entrySet()) {
		    String key = entry.getKey();
		    Object value = entry.getValue();
		    pw.println(value + " ; " + key + "\n");
		}
		pw.close();
	}
	
	public static void writeMapNotJustFacts(String filePath, Map<String, String> lines) throws IOException {
		PrintWriter pw = new PrintWriter(filePath);
		for (Map.Entry<String, String> entry : lines.entrySet()) {
		    String key = entry.getKey();
		    Object value = entry.getValue();
		    pw.println(key + "\n" + value + "\n");
		}
		pw.close();
	}
	
	public static void writeMap_valueList(String filePath, Map<String, Set<String>> lines) throws IOException {
		PrintWriter pw = new PrintWriter(filePath);
		for (Map.Entry<String, Set<String>> entry : lines.entrySet()) {
		    String key = entry.getKey();
		    Set<String> values = entry.getValue();
		    pw.println();
		    pw.println(key);
		    for (String value : values) {
		    	pw.println(value);
			}
		}
		pw.close();
	}
}
