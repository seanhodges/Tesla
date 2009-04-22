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
import android.view.View;
import android.view.View.OnClickListener;

public class Tesla extends Activity implements OnClickListener {
	
	private ICommandService commandService;
	
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			commandService = ICommandService.Stub.asInterface(service);
		}
		
		public void onServiceDisconnected(ComponentName name) {
			commandService = null;
		}
	};
	
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
		bindService(new Intent(Tesla.this, CommandService.class), connection, Context.BIND_AUTO_CREATE);
	}
}