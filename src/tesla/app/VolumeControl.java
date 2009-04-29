package tesla.app;

import tesla.app.command.Command;
import tesla.app.command.CommandFactory;
import tesla.app.service.CommandService;
import tesla.app.service.business.ICommandService;
import tesla.app.widget.VolumeSlider;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

public class VolumeControl extends Activity implements VolumeSlider.OnVolumeLevelChangeListener {
	
	private VolumeSlider volumeSlider;
	private ICommandService commandService;
	
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			commandService = ICommandService.Stub.asInterface(service);
		}
		
		public void onServiceDisconnected(ComponentName name) {
			commandService = null;
		}
	};
	
    /* This is the volume control. */
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.volume_control);
        
        volumeSlider = (VolumeSlider)this.findViewById(R.id.volume);
        volumeSlider.setOnVolumeLevelChangeListener(this);
        
        // Set the initial volume level
        Command command = CommandFactory.instance().getCommand(Command.VOL_CURRENT);
        try {
			command = commandService.sendQuery(command);
		} catch (RemoteException e) {
			// Failed to send query
			e.printStackTrace();
		}
        // Parse the result as a level percentage
        float volumeLevel = Float.parseFloat(command.getOutput());
        if (volumeLevel > 0) {
        	volumeSlider.setLevel((int)(volumeLevel * 100));
        }
        else {
        	volumeSlider.setLevel(0);
        }
    }
    
    private void updateVolume(VolumeSlider volumeSlider, byte level) {
    	Command command = CommandFactory.instance().getCommand(Command.VOL_CHANGE);
    	float levelPercent = (float)level / 100;
		command.addArg(new Float(levelPercent));
		
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

	public void onLevelChanged(VolumeSlider volumeSlider, byte level) {
		updateVolume(volumeSlider, level);
	}
}