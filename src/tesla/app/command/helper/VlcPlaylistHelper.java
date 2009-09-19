package tesla.app.command.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VlcPlaylistHelper implements ICommandHelper {
	
	public static final String MAGIC_MARKER = "[vlcplaylist]";
	
	public String compileQuery(String getPlaylistLength, String getEntryMetadata, boolean includeMarker) {
		StringBuilder builder = new StringBuilder();
		if (includeMarker) {
			builder.append("echo " + MAGIC_MARKER + ";");
		}
		// Query for each track in track list, and dump DBUS array
		builder.append("for (( i=1; i<=$(" + getPlaylistLength + "); i++ )); do ");
		builder.append(getEntryMetadata + " int32:$i; ");
		builder.append("done");
		return builder.toString();
	}

	public String compileQuery(String getPlaylistLength, String getEntryMetadata) {
		return compileQuery(getPlaylistLength, getEntryMetadata, true);
	}

	public List<String> evaluateOutputAsList(String rawOut) {
		List<String> out = new ArrayList<String>();
		// Process each DBUS array
		String[] items = rawOut.split("array \\[");
		for (String item : items) {
			// Extract the title and add to the output
			Map<String, String> data = new DBusHelper().evaluateOutputAsMap(item);
			if (data.containsKey("location")) {
				String title = data.get("location");
				// Strip the URI prefix if present
				if (title != null && (title.startsWith("file:/") || title.startsWith("/"))) {
					title = title.substring(title.lastIndexOf("/") + 1);
				}
				out.add(title);
			}
		}
		return out;
	}
	
	public boolean evaluateOutputAsBoolean(String rawOut) {
		return false;
	}

	public String evaluateOutputAsString(String rawOut) {
		return null;
	}

	public Map<String, String> evaluateOutputAsMap(String rawOut) {
		return null;
	}

}
