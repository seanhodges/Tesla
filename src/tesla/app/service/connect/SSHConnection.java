/* Copyright 2009 Sean Hodges <seanhodges@bluebottle.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tesla.app.service.connect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import tesla.app.command.Command;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;

public class SSHConnection implements IConnection  {
	
	private ConnectionOptions config;
	
	private Connection connection;
	private Session session;
	
	private OutputStream stdin;
	InputStream responseStream;
	InputStream errorStream;
	
	public void connect(ConnectionOptions config) throws ConnectionException {
		this.config = config;
		
		// Resolve the IP address 
		boolean pingSuccess = false;
		String address = null; 
		try {
			InetAddress dnsQuery = InetAddress.getByName(config.hostname);
			address = dnsQuery.getHostAddress();
			pingSuccess = dnsQuery.isReachable(3000);
		} catch (UnknownHostException e1) {
			// Address resolution failed
			throw new ConnectionException(ConnectionException.FAILED_AT_CONNECT, config.hostname, "Could not resolve hostname");
		} catch (IOException e) {
			// Ping failed due to network error
			pingSuccess = false;
		}
		
		if (pingSuccess) {
			setupSSHConnection(address);
		}
		else {
			throw new ConnectionException(ConnectionException.FAILED_AT_CONNECT, config.hostname, "Host is not reachable");
		}
	}	
	
	private void setupSSHConnection(String address) throws ConnectionException {
		// Connect to socket
		connection = new Connection(config.hostname, config.port);
		
		// Set up SSH session
		try {
			connection.setCompression(false);
			connection.connect();
		} catch (IOException e) {
			throw new ConnectionException(ConnectionException.FAILED_AT_CONNECT, config.hostname, "Failed to connect to SSH server, is it running?");
		}

		// Authenticate
		try {
			if (!connection.authenticateWithNone(config.username) && connection.isAuthMethodAvailable(config.username, "password")) {
				boolean success = connection.authenticateWithPassword(config.username, config.password);
				if (!success) {
					throw new ConnectionException(ConnectionException.FAILED_AT_AUTH, config.hostname, "Incorrect password for user " + config.username);
				}
			}
		}
		catch (IOException e) {
			// Do nothing, this is captured next
		}
		if (!connection.isAuthenticationComplete()) {
			throw new ConnectionException(ConnectionException.FAILED_AT_AUTH, config.hostname, "Authentication failed for user " + config.username);
		}
		
		// Initialise session
		try {
			session = connection.openSession();
			session.startShell();
			stdin = session.getStdin();
			responseStream = session.getStdout();
			errorStream = session.getStderr();
		} catch (IOException e) {
			throw new ConnectionException(ConnectionException.FAILED_AT_INIT, config.hostname, "SSH session initialisation failed with error: " + e.getMessage());
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
	
	public boolean isConnected() {
		return (session != null);
	}
	
	public synchronized String sendCommand(Command command) throws ConnectionException {
		StringBuffer stdOutBuffer = new StringBuffer();
		if (session != null) {
			try {
				if (responseStream.available() > 0) {
					responseStream.reset();
				}
				if (errorStream.available() > 0) {
					errorStream.reset();
				}
			} catch (Exception e1) {
				// Flushing the streams failed, continue anyway
			}
			try {
				byte[] dataToSend = convertToStdinPacket(command.getCommandString());
				stdin.write(dataToSend);
				stdin.flush();
			} catch (Exception e) {
				throw new ConnectionException(ConnectionException.FAILED_AT_COMMAND, config.hostname, "Failed to send command to client, error returned was: " + e.getMessage());
			}
			
			String stderr = "";
			
			// Keep polling for a response until something comes back
			while (stderr.length() == 0) {
				try {
					// Any STDERR output is treated as a failure for now
					if (errorStream.available() > 0) {
						stderr = getResponseFromSessionStream(errorStream);
						throw new ConnectionException(ConnectionException.FAILED_AT_COMMAND, config.hostname, command.getCommandString() + " :- " + stderr);
					}
					// Read the STDOUT output to return as a response
					String responsePart = getResponseFromSessionStream(responseStream);
					if (responsePart.length() > 0) { 
						stdOutBuffer.append(responsePart);
						int bufferSize = stdOutBuffer.length();
						if (bufferSize > 5) {
							if (stdOutBuffer.substring(bufferSize - 6, bufferSize - 1).equals("[EOC]")) {
								break;
							}
						}
					}
				} catch (Exception e) {
					throw new ConnectionException(ConnectionException.FAILED_AT_COMMAND, config.hostname, command.getCommandString() + " :- " + e);
				}
			}
		}
		else {
			throw new ConnectionException(ConnectionException.FAILED_AT_COMMAND, config.hostname, "Not connected to a server");
		}
		return parseOutput(stdOutBuffer.toString());
	}
	
	private byte[] convertToStdinPacket(String commandString) {
		StringBuilder out = new StringBuilder();
		out.append(commandString);
		if (!commandString.trim().endsWith(";")) {
			out.append(";");
		}
		out.append(" echo \"[EOC]\"");
		out.append("\n");
		return out.toString().getBytes();
	}

	private String getResponseFromSessionStream(InputStream is) throws IOException {
		int available = is.available();
		if (available > 0) {
			byte[] buffer = new byte[available];
			is.read(buffer, 0, available);
			return new String(buffer);
		}
		return "";
	}
	
	private String parseOutput(String data) {
		data = data.replaceAll("\\[EOC\\]\\n", "");
		return data;
	}
}
