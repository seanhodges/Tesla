package tesla.app.command;

import tesla.app.command.provider.AppCommandProvider;
import tesla.app.command.provider.InitScriptProvider;

public class CommandFactory {
	
	private static final long COMMAND_DELAY = 50;
	private static final long INIT_SCRIPT_DELAY = 1000;
	
	private AppCommandProvider config;

	private CommandFactory() {
		config = new AppCommandProvider();
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
		try {
			command = config.queryCommand(key);
		}
		catch (Exception e) {
			// Return a no-op command
			command = null;
		}
		out.setCommandString(command);
		return out;
	}

	public static CommandFactory instance() {
		return new CommandFactory();
	}
}
