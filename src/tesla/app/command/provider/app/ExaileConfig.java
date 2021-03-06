package tesla.app.command.provider.app;

import java.util.HashMap;
import java.util.Map;

import tesla.app.command.Command;
import tesla.app.command.helper.DBusHelper;
import tesla.app.command.helper.ExaileHelper;
import tesla.app.command.provider.IConfigProvider;

public class ExaileConfig implements IConfigProvider {

	private static final String dest = "org.exaile.Exaile";
	private static final String legacyDest = "org.exaile.DBusInterface";
	
	public String getCommand(String key) {
		String out = null;
		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
			String command = new DBusHelper().compileMethodCall(dest, "/org/exaile/Exaile", 
				"org.exaile.Exaile.PlayPause");
			String legacyCommand = new DBusHelper().compileMethodCall(legacyDest, "/DBusInterfaceObject", 
				"org.exaile.DBusInterface.play_pause");
			out = compileCompositeCommand(command, legacyCommand);
		}
		else if (key.equals(Command.PREV)) {
			String command = new DBusHelper().compileMethodCall(dest, "/org/exaile/Exaile", 
				"org.exaile.Exaile.Prev");
			String legacyCommand = new DBusHelper().compileMethodCall(legacyDest, "/DBusInterfaceObject", 
				"org.exaile.DBusInterface.prev_track");
			out = compileCompositeCommand(command, legacyCommand);
		}
		else if (key.equals(Command.NEXT)) {
			String command = new DBusHelper().compileMethodCall(dest, "/org/exaile/Exaile", 
				"org.exaile.Exaile.Next");
			String legacyCommand = new DBusHelper().compileMethodCall(legacyDest, "/DBusInterfaceObject", 
				"org.exaile.DBusInterface.next_track");
			out = compileCompositeCommand(command, legacyCommand);
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
			String command = "echo " + ExaileHelper.MAGIC_MARKER + "; " + new DBusHelper().compileMethodCall(dest, "/org/exaile/Exaile", 
				"org.exaile.Exaile.Query", false);
			String legacyCommand = new DBusHelper().compileMethodCall(legacyDest, "/DBusInterfaceObject", 
				"org.exaile.DBusInterface.get_title", false) + " | grep string | cut -d '\"' -f 2";
			out = compileCompositeCommand(command, legacyCommand);
		}
		else if (key.equals(Command.IS_PLAYING)) {
			String command = new DBusHelper().compileMethodCall(dest, "/org/exaile/Exaile", 
				"org.exaile.Exaile.Query", false) + " | grep 'string' | cut -d ',' -f 1 | cut -d ' ' -f 6";
			String legacyCommand = new DBusHelper().compileMethodCall(legacyDest, "/DBusInterfaceObject", 
				"org.exaile.DBusInterface.status");
			out = compileCompositeCommand(command, legacyCommand);
		}
		
		return out;
	}

	public Map<String, String> getSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.GET_MEDIA_INFO)) {
			settings.put("ENABLED", "true");
		}
		else if (key.equals(Command.IS_PLAYING)) {
			settings.put("ENABLED", "true");
		}
		return settings;
	}
	
	private String compileCompositeCommand(String command, String legacyCommand) {
		StringBuilder builder = new StringBuilder();
		
		// Use legacyCommand if eXaile 0.2.x is present
		builder.append("if [[ \"$(exaile --version)\" =~ \"0.2\" ]]; then ");
		builder.append(legacyCommand);
		
		// Use command otherwise
		builder.append("; else ");
		builder.append(command);
		
		builder.append("; fi");
		return builder.toString();
	}

	public String getLaunchAppCommand() {
		String command = new DBusHelper().compileMethodCall(dest, "/org/exaile/Exaile", 
		"org.exaile.Exaile.Query");
		String legacyCommand = new DBusHelper().compileMethodCall(legacyDest, "/DBusInterfaceObject", 
			"org.exaile.DBusInterface.query");
		String getRunning = compileCompositeCommand(command, legacyCommand);
		StringBuilder builder = new StringBuilder();
		builder.append(getRunning + " &>/dev/null; ");
		builder.append("if [[ $? != 0 ]]; then ");
		builder.append("DISPLAY=:0 exaile &>/dev/null & sleep 5; ");
		builder.append("fi; ");
		builder.append("echo success");
		return builder.toString();
	}
}