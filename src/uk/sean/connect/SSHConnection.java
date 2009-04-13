package uk.sean.connect;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import org.apache.http.ConnectionClosedException;

import jssh.ClientProtocolHandler;
import jssh.DevURandom;
import jssh.ITrueRandom;
import jssh.Options;
import jssh.SSHAuthFailedException;
import jssh.SSHProtocolException;
import jssh.SSHSetupException;
import jssh.STDIN_OutputStream;

public class SSHConnection implements IConnection {
	
	private static final String HOST = "192.168.0.7";
	private static final int PORT = 22;
	private static final String USER = "sean";
	private static final String PASS = "la5TWord5";
	
	private ClientProtocolHandler client = null;
	
	public void connect() throws ConnectionException {
		Options options = new Options();
		options.setHostname(HOST);
		options.setPort(PORT);
		options.setUser(USER);
		options.setDebug(true); // TODO: remove this
		
		// Connect to socket
		InetAddress serverAddress;
		Socket socket;
		try {
			serverAddress = InetAddress.getByName(options.getHostname());
			socket = new Socket(serverAddress, options.getPort());
		} catch (UnknownHostException e) {
			throw new ConnectionException(ConnectionException.FAILED_AT_CONNECT, HOST, "Host is unknown");
		} catch (IOException e) {
			throw new ConnectionException(ConnectionException.FAILED_AT_CONNECT, HOST, "Could not connect to socket, is SSH server running?");
		}
		
		// TODO: Add time-out to authentication process
		
		// Set up SSH session
		try {
			client = new ClientProtocolHandler(socket, options);
			client.exchangeIdStrings();
			client.receiveServerKey();
			ITrueRandom trueRandom = new DevURandom();
			client.sendSessionKey(trueRandom);
		} catch (IOException e) {
			throw new ConnectionException(ConnectionException.FAILED_AT_CONNECT, HOST, "Failed to connect to SSH server, is it running?");
		} catch (SSHSetupException e) {
			throw new ConnectionException(ConnectionException.FAILED_AT_CONNECT, HOST, "Unable to set-up SSH client, check server settings");
		} catch (SSHProtocolException e) {
			throw new ConnectionException(ConnectionException.FAILED_AT_CONNECT, HOST, "Protocol failure, could be a version mismatch");
		}
		
		// Authenticate
		boolean requiresAuth;
		try {
			requiresAuth = client.declareUser(options.getUser());
			if (requiresAuth) {
				boolean success = client.authenticateUser(options.getUser(), PASS);
				if (!success) {
					throw new ConnectionException(ConnectionException.FAILED_AT_AUTH, HOST, "Authentication failed for user " + USER);
				}
			}
		} catch (IOException e) {
			throw new ConnectionException(ConnectionException.FAILED_AT_AUTH, HOST, "Could not get authentication details for user " + USER + " from server");
		} catch (SSHProtocolException e) {
			throw new ConnectionException(ConnectionException.FAILED_AT_AUTH, HOST, "Protocol failure, could be a version mismatch");
		}

		// Initialise session, compression, etc
		try {
			client.preparatoryOperations();
		} catch (Exception e) {
			throw new ConnectionException(ConnectionException.FAILED_AT_INIT, HOST, "Failed to initialise the SSH session");
		}
	}
	
	public String sendCommand(String command) throws ConnectionException {
		String response = null;
		if (client != null) {
			try {
				client.execCmd(command);
			} catch (IOException e) {
				throw new ConnectionException(ConnectionException.FAILED_AT_COMMAND, HOST, command);
			} catch (SSHProtocolException e) {
				throw new ConnectionException(ConnectionException.FAILED_AT_COMMAND, HOST, "Protocol failure, could be a version mismatch");
			}
		}
		else {
			throw new ConnectionException(ConnectionException.FAILED_AT_COMMAND, HOST, "Not connected to a server");
		}
		return response;
	}
}
