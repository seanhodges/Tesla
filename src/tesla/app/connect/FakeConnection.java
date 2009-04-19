package tesla.app.connect;

import tesla.app.command.Command;

public class FakeConnection implements IConnection {

	public void connect(ConnectionOptions config) throws ConnectionException {
		// Do nothing
	}

	public void disconnect() {
		// Do nothing
	}

	public boolean isConnected() {
		return true;
	}

	public String sendCommand(Command command) throws ConnectionException {
		if (command.getKey().equals(Command.INIT)) {
			return "success\n";
		}
		else {
			return "";
		}
	}

}
