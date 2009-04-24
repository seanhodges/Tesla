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
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SeekBar;

public class VolumeControl extends Activity implements OnSeekBarChangeListener {
	
	private VolumeSlider volumeSlider;
	private ICommandService commandService;
	private int lastVolumeLevel = -1;
	
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
        volumeSlider.setOnSeekBarChangeListener(this);
        
        // Set the volume slider levels
        volumeSlider.setMax(10);
        volumeSlider.setLevel(0);
    }
    
    private void updateVolume(SeekBar seekBar, int level) {
    	
    	if (lastVolumeLevel != level) {
    		
        	// Debugging
        	System.out.println(level);
    		
			// Don't pound the service with duplicate volume level requests
			lastVolumeLevel = level;
			
	    	Command command = CommandFactory.instance().getCommand(Command.VOL_CHANGE);
	    	float levelPercent = (float)level / volumeSlider.getMax();
			command.addArg(new Float(levelPercent));
			
			try {
				commandService.sendCommand(command);
			} catch (RemoteException e) {
				// Failed to send command
				e.printStackTrace();
			}
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

	public void onProgressChanged(SeekBar seekBar, int progress,
		boolean fromTouch) {
		int level = ((VolumeSlider) volumeSlider).getLevel();
		updateVolume(seekBar, level);
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// Do nothing
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		// Do nothing
	}
}