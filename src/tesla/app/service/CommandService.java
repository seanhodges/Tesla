package tesla.app.service;

import java.util.Timer;
import java.util.TimerTask;

import tesla.app.command.Command;
import tesla.app.command.CommandFactory;
import tesla.app.connect.ConnectionException;
import tesla.app.connect.ConnectionOptions;
import tesla.app.connect.FakeConnection;
import tesla.app.connect.IConnection;
import tesla.app.connect.SSHConnection;
import tesla.app.service.business.ICommandController;
import tesla.app.service.business.IErrorHandler;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

public class CommandService extends Service {

	private static final int EXEC_POLL_PERIOD = 50;
	
	private final RemoteCallbackList<IErrorHandler> callbacks = new RemoteCallbackList<IErrorHandler>();
	
	private IConnection connection;
	private Timer commandExecutioner;
	private Command nextCommand = null;
	private Command lastCommand = null;
	
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	public IBinder onBind(Intent arg0) {
		return new ICommandController.Stub() {

			// Command delegates
			
			public void sendCommand(Command command) throws RemoteException {
				sendCommandAction(command);
			}
			
			public Command sendQuery(Command command) throws RemoteException {
				return sendQueryAction(command);
			}

			public void registerErrorHandler(IErrorHandler cb) throws RemoteException {
				registerErrorHandlerAction(cb);
			}

			public void unregisterErrorHandler(IErrorHandler cb) throws RemoteException {
				unregisterErrorHandlerAction(cb);
			}
			
		};
	}

	public void onCreate() {
		super.onCreate();
		//connection = new FakeConnection();
		connection = new SSHConnection();
		
        try {
			connection.connect(new ConnectionOptions(this));
			// Initialise the DBUS connection
			String response = connection.sendCommand(CommandFactory.instance().getInitScript());
			if (!response.equals("success\n")) {
				throw new Exception("Init script failed with output: " + response);
			}
			
			// Start the command thread
			handleCommands();
			
		} catch (Exception e) {
			// Show errors in a dialog
			new AlertDialog.Builder(CommandService.this)
	        	.setTitle("Failed to connect to remote machine")
	        	.setMessage(e.getMessage())
	        	.setPositiveButton("Close", new DialogInterface.OnClickListener() {
	        		public void onClick(DialogInterface dialog, int whichButton) {
	        			// Return the user to the connection screen
	        			stopSelf();
	        		}
	        	})
	        	.show();
		}
	}

	public void onDestroy() {
		super.onDestroy();
		if (commandExecutioner != null) commandExecutioner.cancel();
		if (connection.isConnected()) connection.disconnect();
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
						connection.sendCommand(nextCommand);
						if (connection instanceof FakeConnection) {
							// Display the command for debugging
							System.out.println("FakeConnection: command received: " + nextCommand.getCommandString());
						}
					}
					catch (Exception e) {
						try {
							broadcastError("Failed to send command to remote machine", e);
						} catch (RemoteException e1) {
							// TODO: Don't swallow the asynchronous RemoteExceptions
							e1.printStackTrace();
						}
					}
				}
			}
			
		}, 0, EXEC_POLL_PERIOD);
	}

	protected void sendCommandAction(Command command) {
		if (command != null) {
			nextCommand = command;
		}
	}
	
	protected Command sendQueryAction(Command command) throws RemoteException {
		// Queries are returned synchronously
		if (command != null) {
			try {
				String stdOut = connection.sendCommand(command);
				command.setOutput(stdOut);
			} catch (ConnectionException e) {
				broadcastError("Failed to send query to remote machine", e);
				command.setOutput("");
			}
			if (connection instanceof FakeConnection) {
				// Display the command for debugging
				System.out.println("FakeConnection: query received: " + command.getCommandString() + ", result: " + command.getOutput());
			}
		}
		return command;
	}

	private void broadcastError(String description, Exception e) throws RemoteException {
		int callbackCount = callbacks.beginBroadcast();
		for (int it = 0; it < callbackCount; it++) {
			callbacks.getBroadcastItem(it).onServiceError(
				description, 
				e.getMessage(), 
				false);
		}
	}

	protected void unregisterErrorHandlerAction(IErrorHandler cb) {
		if (cb != null) callbacks.unregister(cb);
	}

	protected void registerErrorHandlerAction(IErrorHandler cb) {
		if (cb != null) callbacks.register(cb);
	}
}
