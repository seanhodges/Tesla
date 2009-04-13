package uk.sean.connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;

public class SSHConnection implements IConnection {
	
	private static final String HOST = "192.168.0.7";
	private static final int PORT = 22;
	private static final String USER = "sean";
	private static final String PASS = "la5TWord5";
	
	private Connection connection;
	private Session session;
	private OutputStream stdin;
	InputStream responseStream;
	InputStream errorStream;
	
	public void connect() throws ConnectionException {
		
		// Connect to socket
		connection = new Connection(HOST, PORT);
		
		// Set up SSH session
		try {
			connection.setCompression(false);
			connection.connect();
		} catch (IOException e) {
			throw new ConnectionException(ConnectionException.FAILED_AT_CONNECT, HOST, "Failed to connect to SSH server, is it running?");
		}

		// Authenticate
		try {
			if (!connection.authenticateWithNone(USER) && connection.isAuthMethodAvailable(USER, "password")) {
				boolean success = connection.authenticateWithPassword(USER, PASS);
				if (!success) {
					throw new ConnectionException(ConnectionException.FAILED_AT_AUTH, HOST, "Incorrect password for user " + USER);
				}
			}
		}
		catch (IOException e) {
			// Do nothing, this is captured next
		}
		if (!connection.isAuthenticationComplete()) {
			throw new ConnectionException(ConnectionException.FAILED_AT_AUTH, HOST, "Authentication failed for user " + USER);
		}
		
		// Initialise session
		try {
			session = connection.openSession();
			session.startShell();
			stdin = session.getStdin();
			responseStream = session.getStdout();
			errorStream = session.getStderr();
		} catch (IOException e) {
			throw new ConnectionException(ConnectionException.FAILED_AT_INIT, HOST, "SSH session initialisation failed with error: " + e.getMessage());
		}
		
	}
	
	public void disconnect() {
		try {
            stdin.close();
            responseStream.close();
            errorStream.close();
        } catch (IOException e) {
        	// These will be forcefully closed by the session
        }
		if (session != null) session.close();
		connection.close();
	}
	
	public String sendCommand(String command) throws ConnectionException {
		String response = null;
		if (session != null) {
			try {
				stdin.write((command + "\n").getBytes());
				stdin.flush();
				Thread.sleep(1000);
			} catch (Exception e) {
				throw new ConnectionException(ConnectionException.FAILED_AT_COMMAND, HOST, "Failed to send command to client, error returned was: " + e.getMessage());
			}
			
			try {
				// Any STDERR output is treated as a failure for now
				if (errorStream.available() > 0) {
					String stderr = getResponseFromSessionStream(errorStream);
					throw new ConnectionException(ConnectionException.FAILED_AT_COMMAND, HOST, command + " :- " + stderr);
				}
				// Read the STDOUT output to return as a response
				response = getResponseFromSessionStream(responseStream);
			} catch (IOException e) {
				throw new ConnectionException(ConnectionException.FAILED_AT_COMMAND, HOST, command);
			}
		}
		else {
			throw new ConnectionException(ConnectionException.FAILED_AT_COMMAND, HOST, "Not connected to a server");
		}
		return response;
	}
	
	private String getResponseFromSessionStream(InputStream is) throws IOException {
		if (is.available() > 0) {
			byte[] buffer = new byte[is.available()];
			is.read(buffer, 0 ,is.available());
			return new String(buffer);
		}
		return "";
	}
}
