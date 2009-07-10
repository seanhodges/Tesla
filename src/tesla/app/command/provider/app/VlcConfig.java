package tesla.app.command.provider.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tesla.app.command.Command;
import tesla.app.command.helper.DBusHelper;
import tesla.app.command.provider.IConfigProvider;

public class VlcConfig implements IConfigProvider {

	public String getCommand(String key) {
		final String dest = "org.mpris.vlc";
		List<String> args = new ArrayList<String>();
		String out = null;
		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.Pause");
		}
		else if (key.equals(Command.PREV)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.Prev");
		}
		else if (key.equals(Command.NEXT)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.Next");
		}
		else if (key.equals(Command.VOL_CHANGE)) {
			args.add(new DBusHelper().evaluateArg("%i"));
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.VolumeSet", args);
		}
		else if (key.equals(Command.VOL_MUTE)) {
			args.add(new DBusHelper().evaluateArg("0"));
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.VolumeSet", args);
		}
		else if (key.equals(Command.VOL_CURRENT)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.VolumeGet");
		}
		return out;
	}

	public Map<String, String> getSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "25.0");
		}
		return settings;
	}

}
