package tesla.app;

import tesla.app.command.Command;
import tesla.app.command.CommandFactory;
import tesla.app.service.CommandService;
import tesla.app.service.business.ICommandService;
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
	
	private SeekBar volumeSlider;
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
        
        volumeSlider = (SeekBar)this.findViewById(R.id.volume);
        volumeSlider.setOnSeekBarChangeListener(this);
        
        // TODO: Bind to service
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
		int level = ((SeekBar) volumeSlider).getProgress();
		Command command = CommandFactory.instance().getCommand(Command.VOL_CHANGE);
		command.addArg(new Float((float)level / 100));
		
		try {
			commandService.sendCommand(command);
		} catch (RemoteException e) {
			// Failed to send command
			e.printStackTrace();
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// Do nothing
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		// Do nothing
	}
}