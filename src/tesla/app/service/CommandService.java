package tesla.app.service;

import tesla.app.command.Command;
import tesla.app.command.CommandFactory;
import tesla.app.connect.ConnectionOptions;
import tesla.app.connect.FakeConnection;
import tesla.app.connect.IConnection;
import tesla.app.connect.SSHConnection;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;

public class CommandService extends Service {

	private IConnection connection;
	
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	public IBinder onBind(Intent arg0) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		/*if (System.getProperty("connection").equals("fake")) {
			connection = new FakeConnection();
		}
		else {*/
			connection = new SSHConnection();
		//}
		
        try {
			connection.connect(new ConnectionOptions(this));
			// Initialise the DBUS connection
			String response = connection.sendCommand(CommandFactory.instance().getInitScript());
			if (!response.equals("success\n")) {
				throw new Exception("Init script failed with output: " + response);
			}
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
		if (connection.isConnected()) connection.disconnect();
	}

	public void sendCommand(Command command) {
		if (command != null) {
			try {
				connection.sendCommand(command);
				if (connection instanceof FakeConnection) {
					// Display the command for debugging
					System.out.println("FakeConnection: command recieved" + command.getCommandString());
				}
			} catch (Exception e) {
				// Show errors in a dialog
				new AlertDialog.Builder(CommandService.this)
		        	.setTitle("Failed to send command to remote machine")
		        	.setMessage(e.getMessage())
		        	.show();
			}
		}
	}
	
}
