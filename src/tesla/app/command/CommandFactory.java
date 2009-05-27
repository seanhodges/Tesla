package tesla.app.command;

import java.util.Map;

import tesla.app.command.provider.AppConfigProvider;
import tesla.app.command.provider.InitScriptProvider;

public class CommandFactory {
	
	private static final long COMMAND_DELAY = 300;
	private static final long INIT_SCRIPT_DELAY = 1000;
	
	private AppConfigProvider config = null;

	public CommandFactory(String initialApp) {
		config = new AppConfigProvider(initialApp);
	}

	public Command getInitScript() {
		Command out = new Command();
		out.setKey(Command.INIT);
		out.setDelay(INIT_SCRIPT_DELAY);
		out.setCommandString(InitScriptProvider.getInitScript());
		return out;
	}

	public Command getCommand(String key) {
		Command out = new Command();
		out.setKey(key);
		out.setDelay(COMMAND_DELAY);
		String command;
		Map<String, String> settings = null;
		try {
			command = config.getCommand(key);
			settings = config.getSettings(key);
		}
		catch (Exception e) {
			// Return a no-op command
			command = null;
		}
		out.setCommandString(command);
		out.setSettings(settings);
		return out;
	}
}
