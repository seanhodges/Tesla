package tesla.app;

import java.util.Map;

import tesla.app.command.Command;
import tesla.app.command.CommandFactory;
import tesla.app.service.CommandService;
import tesla.app.service.business.ICommandController;
import tesla.app.service.business.IErrorHandler;
import tesla.app.widget.VolumeSlider;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

public class VolumeControl extends Activity implements VolumeSlider.OnVolumeLevelChangeListener {
	
	private VolumeSlider volumeSlider;
	private ICommandController commandService;
	
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			commandService = ICommandController.Stub.asInterface(service);
			// Set the error handling and initial volume level once service connected
			setErrorHandler();
			setInitialVolume();
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
	
	
    /* This is the volume control. */
	
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.volume_control);
        
        volumeSlider = (VolumeSlider)this.findViewById(R.id.volume);
        volumeSlider.setOnVolumeLevelChangeListener(this);
    }
    
    protected void setErrorHandler() {
    	try {
			commandService.registerErrorHandler(errorHandler);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void updateVolume(VolumeSlider volumeSlider, float level) {
		Command command = null;
    	if (level == 0) {
    		command = CommandFactory.instance().getCommand(Command.VOL_MUTE); 
    	}
    	else {
			command = CommandFactory.instance().getCommand(Command.VOL_CHANGE);
	    	Object levelArg = null;
	    	if (level > 1) {
	    		// Values above a fraction should be absolute integers
	    		levelArg = new Integer((int)level);
	    	}
	    	else {
	    		levelArg = new Float(level);
	    	}
			command.addArg(levelArg);
    	}
		
		try {
			commandService.sendCommand(command);
		} catch (RemoteException e) {
			// Failed to send command
			e.printStackTrace();
		}
    }
    
	protected void onPause() {
		super.onPause();
		if (connection != null) unbindService(connection);
	}

	protected void onResume() {
		super.onResume();
		bindService(new Intent(VolumeControl.this, CommandService.class), connection, Context.BIND_AUTO_CREATE);
	}

	public void onLevelChanged(VolumeSlider volumeSlider, float level) {
		updateVolume(volumeSlider, level);
	}
	
	private void setInitialVolume() {
		

		
        Command command = CommandFactory.instance().getCommand(Command.VOL_CURRENT);
        
        Map<String, String> settings = command.getSettings();
        volumeSlider.setMinVolume(Float.parseFloat(settings.get("MIN")));
        volumeSlider.setMaxVolume(Float.parseFloat(settings.get("MAX")));
		volumeSlider.setLevel(0);
		
        try {
			command = commandService.sendQuery(command);
		} catch (RemoteException e) {
			// Failed to send query
			e.printStackTrace();
		}
		
        // Parse the result as a level percentage
		if (command.getOutput() != null && command.getOutput() != "") {
	        float volumeLevel = Float.parseFloat(command.getOutput());
	        if (volumeLevel > 0) {
	        	volumeSlider.setLevel((int)volumeLevel);
	        }
		}
	}
	
	private void onServiceErrorAction(String title, String message, boolean fatal) {
		new AlertDialog.Builder(VolumeControl.this)
			.setTitle(title)
			.setMessage(message)
			.show();
	}
}