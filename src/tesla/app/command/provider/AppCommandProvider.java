package tesla.app.command.provider;

import tesla.app.command.Command;

public class AppCommandProvider {

	public static final String APP_MODE = "amarok";
	
	public String queryCommand(String key) throws Exception {
		
		// These commands will be extracted from
		// a database of application configurations
		
		if (APP_MODE.equals("rhythmbox")) {
			return rhythmBoxCommand(key);
		}
		else {
			return amarokCommand(key);
		}
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
			out = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player setVolume 0";
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
			out = "qdbus org.kde.amarok /Player Play";
		}
		else if (key.equals(Command.PAUSE)) {
			out = "qdbus org.kde.amarok /Player Pause";
		}
		else if (key.equals(Command.PREV)) {
			out = "qqdbus org.kde.amarok /Player Prev";
		}
		else if (key.equals(Command.NEXT)) {
			out = "qdbus org.kde.amarok /Player Next";
		}
		else if (key.equals(Command.VOL_CHANGE)) {
			out = "qdbus org.kde.amarok /Player VolumeSet %f";
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
}
