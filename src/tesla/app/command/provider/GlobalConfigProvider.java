package tesla.app.command.provider;

import java.util.Map;

import tesla.app.command.Command;

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
		return "DISPLAY=:0 gnome-session-save --kill --gui";
	}

	public Map<String, String> getSettings(String key) {
		return null;
	}

}
