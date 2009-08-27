package tesla.app.command.provider.app;

import java.util.HashMap;
import java.util.Map;

import tesla.app.command.Command;
import tesla.app.command.helper.DBusHelper;
import tesla.app.command.helper.ExaileHelper;
import tesla.app.command.provider.IConfigProvider;

public class ExaileConfig implements IConfigProvider {

	public String getCommand(String key) {
		final String dest = "org.exaile.Exaile";
		String out = null;
		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
			out = new DBusHelper().compileMethodCall(dest, "/org/exaile/Exaile", 
				"org.exaile.Exaile.PlayPause");
		}
		else if (key.equals(Command.PREV)) {
			out = new DBusHelper().compileMethodCall(dest, "/org/exaile/Exaile", 
				"org.exaile.Exaile.Prev");
		}
		else if (key.equals(Command.NEXT)) {
			out = new DBusHelper().compileMethodCall(dest, "/org/exaile/Exaile", 
				"org.exaile.Exaile.Next");
		}
		// Volume control does not currently work for Exaile, fall back to system volume
		/* 
		else if (key.equals(Command.VOL_CHANGE)) {
			args.add(new DBusHelper().evaluateArg("%i"));
			out = new DBusHelper().compileMethodCall(dest, "/org/exaile/Exaile", 
				"org.exaile.Exaile.ChangeVolume", args);
		}
		else if (key.equals(Command.VOL_MUTE)) {
			args.add(new DBusHelper().evaluateArg("0"));
			out = new DBusHelper().compileMethodCall(dest, "/org/exaile/Exaile", 
				"org.exaile.Exaile.ChangeVolume", args);
		}
		else if (key.equals(Command.VOL_CURRENT)) {
			out = new DBusHelper().compileMethodCall(dest, "/org/exaile/Exaile", 
				"org.exaile.Exaile.GetVolume");
		}*/
		else if (key.equals(Command.GET_MEDIA_INFO)) {
			out = new DBusHelper().compileMethodCall(dest, "/org/exaile/Exaile", 
				"org.exaile.Exaile.Query", false);
			out = "echo " + ExaileHelper.MAGIC_MARKER + "; " + out;
		}
		else if (key.equals(Command.IS_PLAYING)) {
			out = new DBusHelper().compileMethodCall(dest, "/org/exaile/Exaile", 
				"org.exaile.Exaile.IsPlaying");
		}
		
		return out;
	}

	public Map<String, String> getSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.GET_MEDIA_INFO)) {
			settings.put("ENABLED", "true");
		}
		else if (key.equals(Command.IS_PLAYING)) {
			settings.put("ENABLED", "false");
		}
		return settings;
	}

	public String getLaunchAppCommand() {
		return "pidof exaile 1>/dev/null || exaile &>/dev/null &";
	}
}