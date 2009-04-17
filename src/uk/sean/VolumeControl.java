package uk.sean;

import uk.sean.command.Command;
import uk.sean.command.CommandFactory;
import uk.sean.connect.ConnectionOptions;
import uk.sean.connect.FakeConnection;
import uk.sean.connect.IConnection;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class VolumeControl extends Activity {
	
	private IConnection connection;
	
    /* This is the volume control. */
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.volume_control);
        
        // Attach the button listeners for playback controls
        View volumeSlider = this.findViewById(R.id.volume);
        
        // Assume an SSH connection for now
        //connection = new SSHConnection();
        connection = new FakeConnection();
        try {
			connection.connect(new ConnectionOptions(this));
			// Initialise the DBUS connection
			String response = connection.sendCommand(CommandFactory.instance().getInitScript());
			if (!response.equals("success\n")) {
				throw new Exception("Init script failed with output: " + response);
			}
		} catch (Exception e) {
			// Show errors in a dialog
			new AlertDialog.Builder(VolumeControl.this)
	        	.setTitle("Failed to connect to remote machine")
	        	.setMessage(e.getMessage())
	        	.setPositiveButton("Close", new DialogInterface.OnClickListener() {
	        		public void onClick(DialogInterface dialog, int whichButton) {
	        			// Return the user to the connection screen
	        			finish();
	        		}
	        	})
	        	.show();
		}
    }
    
	protected void onDestroy() {
		super.onDestroy();
		
		// Disconnect from temp connection
		if (connection.isConnected()) connection.disconnect();
	}
}