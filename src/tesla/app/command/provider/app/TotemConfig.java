package tesla.app.command.provider.app;

import java.util.HashMap;
import java.util.Map;

import tesla.app.command.Command;
import tesla.app.command.provider.IConfigProvider;

public class TotemConfig implements IConfigProvider {

	public String getCommand(String key) {
		String out = null;
		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
			out = "DISPLAY=:0 totem --play-pause";
		}
		else if (key.equals(Command.PREV)) {
			out = "DISPLAY=:0 totem --previous";
		}
		else if (key.equals(Command.NEXT)) {
			out = "DISPLAY=:0 totem --next";
		}
		else if (key.equals(Command.VOL_CHANGE)) {
			out = "gconftool-2 -s /apps/totem/volume --type=int %i";
		}
		else if (key.equals(Command.VOL_MUTE)) {
			out = "gconftool-2 -s /apps/totem/volume --type=int 0";
		}
		else if (key.equals(Command.VOL_CURRENT)) {
			out = "gconftool-2 -g /apps/totem/volume";
		}
		
		return out;
	}

	public Map<String, String> getSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "100.0");
		}
		return settings;
	}

}
