package uk.sean;

import uk.sean.connect.ConnectionException;
import uk.sean.connect.IConnection;
import uk.sean.connect.SSHConnection;
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
				connection.connect();
				connection.sendCommand("touch /home/sean/Desktop/tesla_success");
			} catch (ConnectionException e) {
				// Show errors in a dialog
				new AlertDialog.Builder(Tesla.this)
		        	.setTitle("SSH error")
		        	.setMessage(e.getMessage())
		        	.show();
			}
			break;
		}
	}
}