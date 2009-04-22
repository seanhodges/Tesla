package tesla.app;

import tesla.app.connect.ConnectionOptions;
import tesla.app.service.CommandService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class NewConnection extends Activity implements OnClickListener {
	
	ConnectionOptions config;
	
	EditText hostText;
	EditText portText;
	EditText userText;
	EditText passText;
	
    /* This is the new connection screen. */
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.new_connection);
        
        // Attach the button listeners
        View cancelButton = this.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(this);
        View connectButton = this.findViewById(R.id.connect);
        connectButton.setOnClickListener(this);
        
        // Load the last saved settings
        config = new ConnectionOptions(this);
		hostText = (EditText)this.findViewById(R.id.host);
		portText = (EditText)this.findViewById(R.id.port);
		userText = (EditText)this.findViewById(R.id.user);
		passText = (EditText)this.findViewById(R.id.pass);
		hostText.setText(config.hostname);
		portText.setText(String.valueOf(config.port));
		userText.setText(config.username);
		passText.setText(config.password);
		
    }
    
	protected void onDestroy() {
		super.onDestroy();
		stopService(new Intent(NewConnection.this, CommandService.class));
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.connect:
			config.hostname = hostText.getText().toString();
			config.port = Integer.parseInt(portText.getText().toString());
			config.username = userText.getText().toString();
			config.password = passText.getText().toString();
			
			// Check the input
			if (config.port == 0) config.port = 22;
			
			// Save the settings for next time
			config.saveSettings();
			
			// Start the CommandService
			startService(new Intent(NewConnection.this, CommandService.class));
			
			// Start the playback activity
			startActivity(new Intent(NewConnection.this, Tesla.class));
			
			break;
		case R.id.cancel: 
			finish();
			break;
		}
	}
}