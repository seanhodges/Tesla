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
	
	public String compileRecursePlaylistSetCommand(String getCurrentTrack, String gotoPreviousTrack, String gotoNextTrack) {
		// This navigates up or down the track list until the selected track is found 
		// Note: This process is slow, compileRebuildPlaylistSetCommand is a better algorithm
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
	
	public String compileRebuildPlaylistSetCommand(String getPlaylistEntryUri, String dbusDest, String dbusPath) {
		// This rebuilds the playlist, with the selected entry marked for playing immediately
		StringBuilder builder = new StringBuilder();
		builder.append("selectedTrackUri=\"%s\"; ");
		builder.append("listLength=$(dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.GetLength | grep int32 | sed -e \"s/   //\" | cut -d ' ' -f 2); ");
		builder.append("trackUri=\"\"; ");
		builder.append("for (( i=0; i<$listLength; i++ )); do ");
		builder.append("oldTrackUri=$trackUri; ");
		builder.append("until [[ \"$trackUri\" != \"$oldTrackUri\" ]]; do ");
		builder.append("trackUri=$(dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.GetMetadata int32:0 2>/dev/null | sed -e \"s/'/\\'/g\"); ");
		builder.append("trackUri=${trackUri#*location*string*\\\"}; ");
		builder.append("trackUri=${trackUri%%\\\"*)*]}; ");
		builder.append("done; ");
		builder.append("dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.DelTrack int32:0; ");
		builder.append("if [[ \"$selectedTrackUri\" != \"$trackUri\" ]]; then ");
		builder.append("dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.AddTrack string:\"$trackUri\" boolean:false; ");
		builder.append("else ");
		builder.append("dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.AddTrack string:\"$trackUri\" boolean:true; ");
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
