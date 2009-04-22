package tesla.app.service;

import java.util.Timer;
import java.util.TimerTask;

import tesla.app.command.Command;
import tesla.app.command.CommandFactory;
import tesla.app.connect.ConnectionOptions;
import tesla.app.connect.FakeConnection;
import tesla.app.connect.IConnection;
import tesla.app.connect.SSHConnection;
import tesla.app.service.business.ICommandService;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class CommandService extends Service {

	private static final int EXEC_POLL_PERIOD = 10;
	
	private IConnection connection;
	private Timer commandExecutioner;
	private Command nextCommand = null;
	private Command lastCommand = null;
	
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	public IBinder onBind(Intent arg0) {
		return new ICommandService.Stub() {

			// Command delegates
			
			public void sendCommand(Command command) throws RemoteException {
				sendCommandAction(command);
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
	
	public void handleCommands() {
		
		// Commands are executed in a Timer thread
		// this moves them out of the event loop, and drops commands when new ones are requested
		
		commandExecutioner = new Timer();
		commandExecutioner.scheduleAtFixedRate(new TimerTask() {

			public void run() {
				if (nextCommand != null && nextCommand != lastCommand) {
					lastCommand = nextCommand;
					try {
						connection.sendCommand(nextCommand);
						if (connection instanceof FakeConnection) {
							// Display the command for debugging
							System.out.println("FakeConnection: command recieved" + nextCommand.getCommandString());
						}
					}
					catch (Exception e) {
						// Show errors in a dialog
						new AlertDialog.Builder(CommandService.this)
				        	.setTitle("Failed to send command to remote machine")
				        	.setMessage(e.getMessage())
				        	.show();
					}
				}
			}
			
		}, 0, EXEC_POLL_PERIOD);
	}

	public void sendCommandAction(Command command) {
		if (command != null) {
			nextCommand = command;
		}
	}
}
