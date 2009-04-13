package uk.sean.connect;

public interface IConnection {
	public abstract void connect() throws ConnectionException;
	public abstract String sendCommand(String command) throws ConnectionException;
}