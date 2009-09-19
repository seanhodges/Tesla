package tesla.app.command.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RelativePlaylistHelper implements ICommandHelper {
	
	public static final String MAGIC_MARKER = "[relativeplaylist]";
	
	public String compileQuery(String getPlaylistLength, String getEntryMetadata, boolean includeMarker) {
		StringBuilder builder = new StringBuilder();
		if (includeMarker) {
			builder.append("echo " + MAGIC_MARKER + ";");
		}
		// Query for each track in track list, and dump DBUS array
		builder.append("for (( i=0; i<$(" + getPlaylistLength + "); i++ )); do ");
		builder.append(getEntryMetadata + " int32:$i; ");
		builder.append("done");
		return builder.toString();
	}

	public String compileQuery(String getPlaylistLength, String getEntryMetadata) {
		return compileQuery(getPlaylistLength, getEntryMetadata, true);
	}
	
	public String compileSetPlaylistCommand(String getCurrentTrack, String gotoPreviousTrack, String gotoNextTrack) {
		// This navigates up or down the track list until the selected track is found 
		// FIXME: This process is slow, ideally we should be setting the track list selection directly
		StringBuilder builder = new StringBuilder();
		builder.append("selection=%i; ");
		builder.append("playing=$(" + getCurrentTrack + "); ");
		builder.append("until [[ $playing == $selection ]]; do ");
		builder.append("if [[ $playing < $selection ]]; then ");
		builder.append(gotoNextTrack + "; ");
		builder.append("elif [[ $playing > $selection ]]; then ");
		builder.append(gotoPreviousTrack + "; ");
		builder.append("fi; ");
		builder.append("sleep 1; ");
		builder.append("playing=$(" + getCurrentTrack + "); ");
		builder.append("done");
		return builder.toString();
	}

	public List<String> evaluateOutputAsList(String rawOut) {
		List<String> out = new ArrayList<String>();
		// Process each DBUS array
		String[] items = rawOut.split("array \\[");
		for (String item : items) {
			// Extract the title and add to the output
			Map<String, String> data = new DBusHelper().evaluateOutputAsMap(item);
			if (data.containsKey("title")) {
				String title = data.get("title");
				out.add(title);
			}
			else if (data.containsKey("location")) {
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
