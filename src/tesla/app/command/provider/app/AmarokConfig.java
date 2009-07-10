package tesla.app.command.provider.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tesla.app.command.Command;
import tesla.app.command.helper.DBusHelper;
import tesla.app.command.provider.IConfigProvider;
import tesla.app.mediainfo.MediaInfo;

public class AmarokConfig implements IConfigProvider {

	public String getCommand(String key) {
		final String dest = "org.kde.amarok";
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
		else if (key.equals(Command.GET_MEDIA_INFO)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.GetMetadata");
		}
		return out;
	}

	public Map<String, String> getSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "100.0");
		}
		else if (key.equals(Command.GET_MEDIA_INFO)) {
			settings.put("ENABLED", "true");
			settings.put("FORMAT", MediaInfo.FORMAT_DBUS);
		}
		return settings;
	}

}
