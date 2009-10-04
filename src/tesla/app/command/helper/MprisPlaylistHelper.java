package tesla.app.command.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MprisPlaylistHelper implements ICommandHelper {
	
	public static final String MAGIC_MARKER = "[mprisplaylist]";
	
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
	
	public String compileRebuildPlaylistSetCommand(String dbusDest, String dbusPath) {
		// Get playlist info
		List<String> args = new ArrayList<String>();
		String getPlaylistLengthCommand = new DBusHelper().compileMethodCall(dbusDest, dbusPath, 
				"org.freedesktop.MediaPlayer.GetLength", false) + " 2>/dev/null | grep int32 | sed -e 's/   //' | cut -d ' ' -f 2";
		args.add(new DBusHelper().evaluateArg("0"));
		String getTrackUri = new DBusHelper().compileMethodCall(dbusDest, dbusPath,
				"org.freedesktop.MediaPlayer.GetMetadata", args, false) + " 2>/dev/null | sed -e \"s/'/\\'/g\"";
		
		// Add/remove tracks
		String popTrackCommand = new DBusHelper().compileMethodCall(dbusDest, dbusPath,
				"org.freedesktop.MediaPlayer.DelTrack", args, false);
		args.clear();
		args.add("string:\"$trackUri\"");
		args.add(new DBusHelper().evaluateArg("false"));
		String pushTrackCommand = new DBusHelper().compileMethodCall(dbusDest, dbusPath,
				"org.freedesktop.MediaPlayer.AddTrack", args, false);
		args.clear();
		args.add("string:\"$trackUri\"");
		args.add(new DBusHelper().evaluateArg("true"));
		String pushPlayingTrackCommand = new DBusHelper().compileMethodCall(dbusDest, dbusPath,
				"org.freedesktop.MediaPlayer.AddTrack", args, false);
		
		// Build the script
		StringBuilder builder = new StringBuilder();
		builder.append("selectedTrackUri=\"%s\"; ");
		builder.append("listLength=$(" + getPlaylistLengthCommand + "); ");
		builder.append("trackUri=\"\"; ");
		builder.append("for (( i=0; i<$listLength; i++ )); do ");
		builder.append("oldTrackUri=$trackUri; ");
		builder.append("until [[ \"$trackUri\" != \"$oldTrackUri\" ]]; do ");
		builder.append("trackUri=$(" + getTrackUri + "); ");
		builder.append("trackUri=${trackUri#*location*string*\\\"}; ");
		builder.append("trackUri=${trackUri%%\\\"*)*]}; ");
		builder.append("done; ");
		builder.append(popTrackCommand + "; ");
		builder.append("if [[ \"$selectedTrackUri\" != \"$trackUri\" ]]; then ");
		builder.append(pushTrackCommand + " &>/dev/null; ");
		builder.append("else ");
		builder.append(pushPlayingTrackCommand + " &>/dev/null; ");
		builder.append("fi; done");
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
