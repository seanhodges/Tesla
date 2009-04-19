package uk.sean;

import uk.sean.command.Command;
import uk.sean.command.CommandFactory;
import uk.sean.connect.ConnectionOptions;
import uk.sean.connect.FakeConnection;
import uk.sean.connect.IConnection;
import uk.sean.connect.SSHConnection;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class Tesla extends Activity implements OnClickListener {
	
	private IConnection connection;
	
    /* This is the main screen, providing the playback controls. */
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        // Attach the button listeners for playback controls
        View playPauseButton = this.findViewById(R.id.play_pause);
        playPauseButton.setOnClickListener(this);
        View prevSongButton = this.findViewById(R.id.last_song);
        prevSongButton.setOnClickListener(this);
        View nextSongButton = this.findViewById(R.id.next_song);
        nextSongButton.setOnClickListener(this);
        View volumeButton = this.findViewById(R.id.volume);
        volumeButton.setOnClickListener(this);
        
        // Assume an SSH connection for now
        connection = new SSHConnection();
        //connection = new FakeConnection();
        try {
			connection.connect(new ConnectionOptions(this));
			// Initialise the DBUS connection
			String response = connection.sendCommand(CommandFactory.instance().getInitScript());
			if (!response.equals("success\n")) {
				throw new Exception("Init script failed with output: " + response);
			}
		} catch (Exception e) {
			// Show errors in a dialog
			new AlertDialog.Builder(Tesla.this)
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
	
	public void onClick(View v) {
		Command command = null;
		
		switch (v.getId()) {
		case R.id.play_pause: 
			command = CommandFactory.instance().getCommand(Command.PLAY);
			break;
		case R.id.last_song:
			command = CommandFactory.instance().getCommand(Command.PREV);
			break;
		case R.id.next_song:
			command = CommandFactory.instance().getCommand(Command.NEXT);
			break;
		case R.id.volume:
			// Start the volume control activity
			Intent intent = new Intent(Tesla.this, VolumeControl.class);
			startActivity(intent);
		}
		
		if (command != null) {
			try {
				connection.sendCommand(command);
				if (connection instanceof FakeConnection) {
					// Display the command for debugging
					new AlertDialog.Builder(Tesla.this)
						.setTitle("FakeConnection: command recieved")
						.setMessage(command.getCommandString())
						.show();
				}
			} catch (Exception e) {
				// Show errors in a dialog
				new AlertDialog.Builder(Tesla.this)
		        	.setTitle("Failed to send command to remote machine")
		        	.setMessage(e.getMessage())
		        	.show();
			}
		}
	}
}