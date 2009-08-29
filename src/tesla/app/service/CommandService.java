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

package tesla.app.service;

import java.util.Timer;
import java.util.TimerTask;

import tesla.app.command.Command;
import tesla.app.command.CommandFactory;
import tesla.app.service.business.ICommandController;
import tesla.app.service.business.IErrorHandler;
import tesla.app.service.connect.ConnectionException;
import tesla.app.service.connect.ConnectionOptions;
import tesla.app.service.connect.FakeConnection;
import tesla.app.service.connect.IConnection;
import tesla.app.service.connect.SSHConnection;
import tesla.app.service.connect.ConnectionOptions.ConnectMode;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

public class CommandService extends Service {

	private static final int EXEC_POLL_PERIOD = 100; // Cycles
	
	private final RemoteCallbackList<IErrorHandler> callbacks = new RemoteCallbackList<IErrorHandler>();
	
	private volatile IConnection connection;
	private volatile CommandFactory factory;
	
	private Timer commandExecutioner;
	private Command nextCommand = null;
	private Command lastCommand = null;
	private ConnectionOptions connectOptions;
	
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		connectOptions = new ConnectionOptions(this);
		
		if (connectOptions.mode == ConnectMode.FAKE) {
			connection = new FakeConnection();
		}
		else {
			connection = new SSHConnection();
		}
		
		factory = new CommandFactory(connectOptions.appSelection);
	}

	public IBinder onBind(Intent arg0) {
		return new ICommandController.Stub() {

			// Command delegates

			public boolean connect() throws RemoteException {
				return connectAction();
			}
			
			public Command queryForCommand(String key, boolean ignoreAppCommand) throws RemoteException {
				return queryForCommandAction(key, ignoreAppCommand);
			}
			
			public void sendCommand(Command command) throws RemoteException {
				sendCommandAction(command);
			}
			
			public Command sendQuery(Command command) throws RemoteException {
				return sendQueryAction(command);
			}
			
			public void reloadCommandFactory() throws RemoteException {
				reloadCommandFactoryAction();
			}

			public void registerErrorHandler(IErrorHandler cb) throws RemoteException {
				registerErrorHandlerAction(cb);
			}

			public void unregisterErrorHandler(IErrorHandler cb) throws RemoteException {
				unregisterErrorHandlerAction(cb);
			}
			
		};
	}
	
	public synchronized boolean connectAction() throws RemoteException {
		boolean success = false;
        try {
        	// Ignore fake connections, and loopback connections (for emulator)
        	if (connectOptions.mode == ConnectMode.SSH 
        			&& !connectOptions.hostname.equals("10.0.2.2")) {
        		// Start the wifi service if it is not running already
        		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    			wifi.setWifiEnabled(true);
				// Poll for successful WIFI connection
				while (!wifi.isWifiEnabled()
						|| wifi.getConnectionInfo().getIpAddress() <= 0 
						|| wifi.getConnectionInfo().getSSID() == null
						|| wifi.getConnectionInfo().getBSSID() == null) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
        	}
        	
			connection.connect(connectOptions);
			
			// Initialise the DBUS connection
			String response = connection.sendCommand(factory.getInitScript());
			if (!response.equals("success\n")) {
				throw new Exception("Init script failed with output: " + response);
			}
			
			// Start the target player if not already running
			response = connection.sendCommand(factory.getLaunchAppCommand());
			
			// Start the command thread
			handleCommands();
			success = true;
			
		} catch (Exception e) {
			broadcastError("Failed to connect to remote machine", e, true);
			stopSelf();
		}
		return success;
	}

	public void onDestroy() {
		super.onDestroy();
		if (commandExecutioner != null) commandExecutioner.cancel();
		if (connection != null && connection.isConnected()) connection.disconnect();
	}
	
	private void handleCommands() {
		
		// Commands are executed in a Timer thread
		// this moves them out of the event loop, and drops commands when new ones are requested
		
		commandExecutioner = new Timer();
		commandExecutioner.schedule(new TimerTask() {

			public void run() {
				if (nextCommand != null && nextCommand != lastCommand) {
					lastCommand = nextCommand;
					try {
						if (!nextCommand.getCommandString().equals("")) {
							connection.sendCommand(nextCommand);
						}
						
						if (connection instanceof FakeConnection) {
							// Display the command for debugging
							System.out.println("FakeConnection: command received: " + nextCommand.getCommandString());
						}
					}
					catch (Exception e) {
						try {
							broadcastError("Failed to send command to remote machine", e, false);
						} catch (RemoteException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
			
		}, 0, EXEC_POLL_PERIOD);
	}
	
	protected Command queryForCommandAction(String key, boolean ignoreAppCommand) {
		return factory.getCommand(key, ignoreAppCommand);
	}

	protected synchronized void sendCommandAction(Command command) {
		if (command != null) {
			nextCommand = command;
		}
	}
	
	protected synchronized Command sendQueryAction(Command command) throws RemoteException {
		// Queries are returned synchronously
		if (command != null) {
			try {
				String stdOut = connection.sendCommand(command);
				command.setOutput(stdOut);
			} catch (ConnectionException e) {
				broadcastError("Failed to send query to remote machine", e, false);
				command.setOutput("");
			}
			if (connection instanceof FakeConnection) {
				// Display the command for debugging
				System.out.println("FakeConnection: query received: " + command.getCommandString() + ", result: " + command.getOutput());
			}
		}
		return command;
	}

	private void broadcastError(String description, Exception e, boolean fatal) throws RemoteException {
		int callbackCount = callbacks.beginBroadcast();
		for (int it = 0; it < callbackCount; it++) {
			callbacks.getBroadcastItem(it).onServiceError(
				description, 
				e.getMessage(), 
				fatal);
		}
	}

	protected void unregisterErrorHandlerAction(IErrorHandler cb) {
		if (cb != null) callbacks.unregister(cb);
	}

	protected void registerErrorHandlerAction(IErrorHandler cb) {
		if (cb != null) callbacks.register(cb);
	}

	private void reloadCommandFactoryAction() {
		connectOptions.reloadSettings();
		factory = new CommandFactory(connectOptions.appSelection);
	}
}
