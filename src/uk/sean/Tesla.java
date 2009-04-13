package uk.sean;

import uk.sean.connect.ConnectionException;
import uk.sean.connect.IConnection;
import uk.sean.connect.SSHConnection;
import uk.sean.dbus.InitScriptProvider;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class Tesla extends Activity implements OnClickListener {
	
    /* This is the main screen, providing the playback controls. */
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        View playPauseButton = this.findViewById(R.id.play_pause);
        playPauseButton.setOnClickListener(this);
    }

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.play_pause:
			IConnection connection = new SSHConnection();
			try {
				String response = null;
				connection.connect();
				// Initialise the DBUS connection
				response = connection.sendCommand(InitScriptProvider.getInitScript());
				if (!response.equals("success\n")) {
					throw new Exception("Init script failed with output: " + response);
				}
				
				// Send a qdbus command as a test
				response = connection.sendCommand("qdbus");
				new AlertDialog.Builder(Tesla.this)
		        	.setTitle("Response")
		        	.setMessage(response)
		        	.show();
			} catch (Exception e) {
				// Show errors in a dialog
				new AlertDialog.Builder(Tesla.this)
		        	.setTitle("Error")
		        	.setMessage(e.getMessage())
		        	.show();
			}
			finally {
				connection.disconnect();
			}
			break;
		}
	}
}