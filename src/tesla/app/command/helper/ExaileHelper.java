package tesla.app.command.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.*;

public class ExaileHelper implements ICommandHelper {

	public static final String MAGIC_MARKER = "[exaile]";
	
	public boolean evaluateOutputAsBoolean(String rawOut) {
		return false;
	}

	public Map<String, String> evaluateOutputAsMap(String rawOut) {
		rawOut = rawOut.trim();
		Map<String, String> output;
		output = new HashMap<String, String>();
		output.put("artist", getFromQuery(rawOut, "artist"));
		output.put("album", getFromQuery(rawOut, "album"));
		output.put("title", getFromQuery(rawOut, "title"));
		output.put("tracknumber", getFromQuery(rawOut, "track"));
		return output;
	}

	public String evaluateOutputAsString(String rawOut) {
		return null;
	}
	
	private String getFromQuery(String query, String target) {
		String value = null;
		if (target.equals("title")) {
			Pattern pattern = Pattern.compile("title:.([\\w\\s\\W]+),.artist");
			Matcher matcher = pattern.matcher(query);
			while (matcher.find()) { 
				value = matcher.group(1);
			}
		}
		else if (target.equals("artist")) {
			Pattern pattern = Pattern.compile("artist:.([\\w\\s\\W]+),.album");
			Matcher matcher = pattern.matcher(query);
			while (matcher.find()) { 
				value = matcher.group(1);
			}
		}
		else if (target.equals("album")) {
			Pattern pattern = Pattern.compile("album:.([\\w\\s\\W]+),.length");
			Matcher matcher = pattern.matcher(query);
			while (matcher.find()) { 
				value = matcher.group(1);
			}
		}
		else if (target == "track") {
			value = null;
		}
		return value;
	}

	public List<String> evaluateOutputAsList(String rawOut) {
		return null;
	}
}
