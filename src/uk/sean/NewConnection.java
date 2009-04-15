package uk.sean;

import uk.sean.connect.ConnectionException;
import uk.sean.connect.ConnectionOptions;
import uk.sean.connect.IConnection;
import uk.sean.connect.SSHConnection;
import uk.sean.dbus.InitScriptProvider;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class NewConnection extends Activity implements OnClickListener {
	
	private IConnection connection;
	
    /* This is the new connection screen. */
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.new_connection);
        
        // Attach the button listeners for playback controls
        View cancelButton = this.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(this);
        View connectButton = this.findViewById(R.id.connect);
        connectButton.setOnClickListener(this);
        
    }
    
	protected void onDestroy() {
		super.onDestroy();
		// Disconnect from temp connection
		connection.disconnect();
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.connect:
			
			EditText userText = (EditText)this.findViewById(R.id.user);
			EditText passText = (EditText)this.findViewById(R.id.pass);
			EditText hostText = (EditText)this.findViewById(R.id.host);
			EditText portText = (EditText)this.findViewById(R.id.port);
			
			ConnectionOptions config = new ConnectionOptions();
			config.hostname = hostText.getText().toString();
			config.port = Integer.parseInt(portText.getText().toString());
			config.username = userText.getText().toString();
			config.password = passText.getText().toString();
			
			// Check the input
			if (config.port == 0) config.port = 22;
			
			// Assume an SSH connection for now
			connection = new SSHConnection();
	        try {
				connection.connect(config);
				// Initialise the DBUS connection
				String response = connection.sendCommand(InitScriptProvider.getInitScript());
				if (!response.equals("success\n")) {
					throw new Exception("Init script failed with output: " + response);
				}
			} catch (Exception e) {
				// Show errors in a dialog
				new AlertDialog.Builder(NewConnection.this)
		        	.setTitle("Failed to connect to remote machine")
		        	.setMessage(e.getMessage())
		        	.show();
				connection.disconnect();
			}
			break;
		case R.id.cancel: 
			finish();
			break;
		}
	}
}