package translator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * A few JSON utilities taken from {@link http://saada.u-strasbg.fr Saada} 
 * @author laurentmichel
 * @version $Id$
 *
 */
public abstract class JsonUtils {
	static boolean STDOUT = true;
	
	/**
	 * Produces a JSON object interpreted by the client as an error message
	 * @param msg Error message
	 * @return    Returns the JSON string
	 */
	public static String getErrorMsg(String msg) {
		return "{ \"errormsg\": \"" + msg.replaceAll("\"", "'") + "\"}";
	}
	
	/**
	 * Return a JSON string made with treepath.
	 * treepath has the form filed1SEPARfiled2SEPA... where SERAP is either a " ", a ; or a :
	 * @param treepath
	 * @return JSON string as {path:[field1, .....fieldsn]}
	 */
	@SuppressWarnings("unchecked")
	public static final String convertTreeNode(String treepath) {
		JSONObject retour = new JSONObject();
		JSONArray path = new JSONArray();
		if( treepath != null   ) {
			String[] pts = treepath.split("[\\s;:]");
			for( String pt: pts) {
				if( pt.length() > 0)
					path.add(pt);
			}
		}
		retour.put("path", path);
		return retour.toJSONString();
	}
	
	/**
	 * Push the message into the output stream and copy it on stdout if the flag @see STDOUT is true
	 * @param out          output stream
	 * @param msg          message to process
	 * @throws IOException 
	 */
	public static void teePrint(ServletOutputStream out, String msg) throws IOException {
		if( STDOUT ) System.out.println(msg);
		out.println(msg);
	}
	
	/**
	 * Returns the value of the field in the Json files. The field can be a doted path.
	 * We suppose that all field components are atomic (no array)
	 * @param jsonFile     Input JSON file
	 * @param Field        JSON object field we want the value
	 * @return             The field value
	 * @throws Exception   If the field is not found or if the file can not be read
	 */
	public static String getValue(String jsonFile, String Field) throws Exception {
		String [] Fields = Field.split("\\.");
		BufferedReader in = new BufferedReader(new FileReader(jsonFile));
		String str;
		StringBuffer sb =new StringBuffer();
		while ((str = in.readLine()) != null) {
			sb.append(str);
		}
		in.close();
		Object obj=JSONValue.parse(sb.toString());
		for( String f: Fields) {
			obj = ((JSONObject)obj).get(f);
		}
		return  obj.toString();
	}

	/**
	 * Returns all values of field in the Json files. 
	 * @param jsonFile     Input JSON file
	 * @param Field        JSON object field we want the value
	 * @return             The field value
	 * @throws Exception   If the field is not found or if the file can not be read
	 */	public static String[] getValues(String jsonFile, String Field) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(jsonFile));
		String str;
		StringBuffer sb =new StringBuffer();
		while ((str = in.readLine()) != null) {
			sb.append(str);
		}
		in.close();
		return JsonKeyFinder.findKeys(sb.toString(), Field);
	}
		public static String getParam(String param, Object value, String indentation) {
			return indentation + "\"" + param + "\": \"" + value + "\"";
		}
		public static String getParam(String param, Object value) {
			return getParam(param, value, "");
		}

}
