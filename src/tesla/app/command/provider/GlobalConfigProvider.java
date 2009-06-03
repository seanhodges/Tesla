package tesla.app.command.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tesla.app.command.Command;
import tesla.app.command.helper.DBusHelper;

public class GlobalConfigProvider implements IConfigProvider {

	public String getCommand(String key) throws Exception {
		String out = null;
		
		if (key.equals(Command.POWER)) {
			out = powerCommand();
		}
		
		return out;
	}

	private String powerCommand() {
		// TODO: make this desktop agnostic
		
		List<String> args = new ArrayList<String>();
		args.add(new DBusHelper().evaluateArg("-1")); // Confirmation (-1 : user-defined, 0 : immediate, 1 : prompt user)
		args.add(new DBusHelper().evaluateArg("2")); // Logout type (-1 : previous, 0 : logout, 1 : reboot, 2 : halt)
		// Session handling (-1 : previous, 0 : wait for other sessions, 1 : cancel if other sessions active, 2 : force, 3 : prompt user)
		args.add(new DBusHelper().evaluateArg("3"));
		
		return new DBusHelper().compileMethodCall("org.kde.ksmserver", "/KSMServer", 
				"org.kde.KSMServerInterface.logout", args);
		//return "DISPLAY=:0 gnome-session-save --kill --gui";
	}

	public Map<String, String> getSettings(String key) {
		return null;
	}

}
