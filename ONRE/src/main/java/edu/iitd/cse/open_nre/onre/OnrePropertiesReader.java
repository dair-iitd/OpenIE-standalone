package edu.iitd.cse.open_nre.onre;

/**
 * 
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import edu.iitd.cse.open_nre.onre.constants.OnreFilePaths;

/**
 * @author harinder
 *
 */
public class OnrePropertiesReader {

	private static final String	PROP_COLLAPSE_GRAPH	     = "collapseGraph";
	private static final String	PROP_SIMPLIFY_POSTAGS	 = "simplifyPostags";
	private static final String	PROP_SIMPLIFY_VB_POSTAGS	= "simplifyVBPostags";
	private static Properties	prop;

	private static boolean	    propertiesRead	         = false;

	public static void readProperties() {
		prop = new Properties();
		InputStream input = null;

		try {

			input = OnrePropertiesReader.class.getResourceAsStream(OnreFilePaths.filePath_properties);
			prop.load(input);
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					System.err.println("Problem while closing bufferedReader - Continuing");
				}
			}
		}
	}

	public static boolean isCollapseGraph() {
		checkIfPropertiesRead();

		String collapseGraphVal = prop.getProperty(PROP_COLLAPSE_GRAPH);
		return collapseGraphVal.equalsIgnoreCase("true");
	}

	public static boolean isSimplifyPostags() {
		checkIfPropertiesRead();

		String simplifyPostagsVal = prop.getProperty(PROP_SIMPLIFY_POSTAGS);
		return simplifyPostagsVal.equalsIgnoreCase("true");
	}

	public static boolean isSimplifyVBPostags() {
		checkIfPropertiesRead();

		String simplifyVbPostagsVal = prop.getProperty(PROP_SIMPLIFY_VB_POSTAGS);
		return simplifyVbPostagsVal.equalsIgnoreCase("true");
	}

	private static void checkIfPropertiesRead() {
		if (!propertiesRead) {
			readProperties();
			propertiesRead = true;
		}
	}
}
