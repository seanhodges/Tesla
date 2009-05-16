package tesla.app.command.provider;

import java.util.HashMap;
import java.util.Map;

import tesla.app.command.Command;

public class AppConfigProvider {

	public static final String APP_RHYTHMBOX = "rhythmbox";
	public static final String APP_AMAROK = "amarok";
	public static final String APP_VLC = "vlc";
	
	public String appName = "amarok";
	
	public AppConfigProvider(String appName) {
		this.appName = appName;
	}
	
	public String getCommand(String key) throws Exception {
		
		// These commands will be extracted from
		// a database of application configurations
		
		String out = null;
		
		if (appName.equals(APP_RHYTHMBOX)) {
			out = rhythmBoxCommand(key);
		}
		else if (appName.equals(APP_AMAROK)) {
			out = amarokCommand(key);
		}
		else if (appName.equals(APP_VLC)) {
			out = vlcCommand(key);
		}
		
		return out;
	}
	
	public Map<String, String> getSettings(String key) {
		Map<String, String> settings = null;
		
		if (appName.equals(APP_RHYTHMBOX)) {
			settings = rhythmboxSettings(key);
		}
		else if (appName.equals(APP_AMAROK)) {
			settings = amarokSettings(key);
		}
		else if (appName.equals(APP_VLC)) {
			settings = vlcSettings(key);
		}
		
		return settings;
	}
	
	private Map<String, String> rhythmboxSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "1.0");
		}
		return settings;
	}
	
	private Map<String, String> amarokSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "100.0");
		}
		return settings;
	}
	
	private Map<String, String> vlcSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "25.0");
		}
		return settings;
	}
	
	private String rhythmBoxCommand(String key) throws Exception {
		String out = "";
		if (key.equals(Command.PLAY)) {
			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player playPause false";
		}
		else if (key.equals(Command.PAUSE)) {
			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player playPause false";
		}
		else if (key.equals(Command.PREV)) {
			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player previous";
		}
		else if (key.equals(Command.NEXT)) {
			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player next";
		}
		else if (key.equals(Command.VOL_CHANGE)) {
			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player setVolume %f";
		}
		else if (key.equals(Command.VOL_MUTE)) {
			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player setVolume 0.0";
		}
		else if (key.equals(Command.VOL_CURRENT)) {
			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player getVolume";
		}
		else {
			throw new Exception("Command not implemented");
		}
		return out;
	}

	private String amarokCommand(String key) throws Exception {
		String out = "";
		if (key.equals(Command.PLAY)) {
			out = "qdbus org.kde.amarok /Player Pause";
		}
		else if (key.equals(Command.PAUSE)) {
			out = "qdbus org.kde.amarok /Player Pause";
		}
		else if (key.equals(Command.PREV)) {
			out = "qdbus org.kde.amarok /Player Prev";
		}
		else if (key.equals(Command.NEXT)) {
			out = "qdbus org.kde.amarok /Player Next";
		}
		else if (key.equals(Command.VOL_CHANGE)) {
			out = "qdbus org.kde.amarok /Player VolumeSet %i";
		}
		else if (key.equals(Command.VOL_MUTE)) {
			out = "qdbus org.kde.amarok /Player VolumeSet 0";
		}
		else if (key.equals(Command.VOL_CURRENT)) {
			out = "qdbus org.kde.amarok /Player VolumeGet";
		}
		else {
			throw new Exception("Command not implemented");
		}
		return out;
	}

	private String vlcCommand(String key) throws Exception {
		String out = "";
		if (key.equals(Command.PLAY)) {
			out = "dbus-send --session --dest=org.mpris.vlc --type=\"method_call\" /Player org.freedesktop.MediaPlayer.Pause";
		}
		else if (key.equals(Command.PAUSE)) {
			out = "dbus-send --session --dest=org.mpris.vlc --type=\"method_call\" /Player org.freedesktop.MediaPlayer.Pause";
		}
		else if (key.equals(Command.PREV)) {
			out = "dbus-send --session --dest=org.mpris.vlc --type=\"method_call\" /Player org.freedesktop.MediaPlayer.Prev";
		}
		else if (key.equals(Command.NEXT)) {
			out = "dbus-send --session --dest=org.mpris.vlc --type=\"method_call\" /Player org.freedesktop.MediaPlayer.Next";
		}
		else if (key.equals(Command.VOL_CHANGE)) {
			out = "dbus-send --session --dest=org.mpris.vlc --type=\"method_call\" /Player org.freedesktop.MediaPlayer.VolumeSet int32:%i";
		}
		else if (key.equals(Command.VOL_MUTE)) {
			out = "dbus-send --session --dest=org.mpris.vlc --type=\"method_call\" /Player org.freedesktop.MediaPlayer.VolumeSet int32:0";
		}
		else if (key.equals(Command.VOL_CURRENT)) {
			out = "qdbus org.mpris.vlc /Player VolumeGet";
			// TODO: dbus-send will need a method of parsing the return data 
			//out = "dbus-send --session --dest=org.mpris.vlc --type=\"method_call\" /Player org.freedesktop.MediaPlayer.VolumeGet";
		}
		else {
			throw new Exception("Command not implemented");
		}
		return out;
	}
}
