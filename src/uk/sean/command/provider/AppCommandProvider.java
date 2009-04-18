package uk.sean.command.provider;

import uk.sean.command.Command;

public class AppCommandProvider {

	public String queryCommand(String key)
				throws Exception {
		// These commands will be extracted from
		// a database of application configurations
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
		else {
			throw new Exception("Command not implemented");
		}
		return out;
	}

}
