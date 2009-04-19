package tesla.app.connect;

import tesla.app.command.Command;

public interface IConnection {
	public abstract void connect(ConnectionOptions config) throws ConnectionException;
	public abstract String sendCommand(Command command) throws ConnectionException;
	public abstract void disconnect();
	public boolean isConnected();
}