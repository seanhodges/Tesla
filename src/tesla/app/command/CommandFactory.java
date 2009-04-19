package tesla.app.command;

import tesla.app.command.provider.AppCommandProvider;
import tesla.app.command.provider.InitScriptProvider;

public class CommandFactory {

	private AppCommandProvider config;

	private CommandFactory() {
		config = new AppCommandProvider();
	}

	public Command getInitScript() {
		Command out = new Command();
		out.setKey(Command.INIT);
		out.setCommandString(InitScriptProvider.getInitScript());
		return out;
	}

	public Command getCommand(String key) {
		Command out = new Command();
		out.setKey(key);
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
