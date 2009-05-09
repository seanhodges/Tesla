package tesla.app;

import tesla.app.connect.ConnectionOptions;
import tesla.app.service.CommandService;
import tesla.app.service.business.ICommandController;
import tesla.app.service.business.IErrorHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class NewConnection extends Activity implements OnClickListener {
	
	ConnectionOptions config;
	
	private ICommandController commandService;
	
	EditText hostText;
	EditText portText;
	EditText userText;
	EditText passText;
	
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			commandService = ICommandController.Stub.asInterface(service);
			// Set the error handling once service connected
			setErrorHandler();
			// Attempt to connect
			boolean success = false;
			try {
				success = commandService.connect();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			// Start the playback activity
			if (success) startActivity(new Intent(NewConnection.this, Tesla.class));
		}
		
		public void onServiceDisconnected(ComponentName name) {
			commandService = null;
		}
	};
	private IErrorHandler errorHandler = new IErrorHandler.Stub() {
		public void onServiceError(String title, String message, boolean fatal) throws RemoteException {
			onServiceErrorAction(title, message, fatal);
		}
	};
	
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
		if (connection != null) {
			unsetErrorHandler();
			unbindService(connection);
		}
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
			
			// Start the CommandService, and attempt to connect it
			startService(new Intent(NewConnection.this, CommandService.class));
			bindService(new Intent(NewConnection.this, CommandService.class), connection, Context.BIND_AUTO_CREATE);
			
			
			break;
		case R.id.cancel: 
			finish();
			break;
		}
	}
	
	protected void setErrorHandler() {
    	try {
			commandService.registerErrorHandler(errorHandler);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	protected void unsetErrorHandler() {
    	try {
			commandService.unregisterErrorHandler(errorHandler);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void onServiceErrorAction(String title, String message, boolean fatal) {
		new AlertDialog.Builder(NewConnection.this)
			.setTitle(title)
			.setMessage(message)
			.show();
	}
}