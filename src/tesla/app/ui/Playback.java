/* Copyright 2009 Sean Hodges <seanhodges@bluebottle.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tesla.app.ui;

import java.io.FileInputStream;
import java.io.InputStream;

import tesla.app.R;
import tesla.app.command.Command;
import tesla.app.mediainfo.MediaInfo;
import tesla.app.service.CommandService;
import tesla.app.service.business.ICommandController;
import tesla.app.service.business.IErrorHandler;
import tesla.app.ui.task.GetMediaInfoTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class Playback extends Activity implements OnClickListener, GetMediaInfoTask.OnGetMediaInfoListener {
	
	protected static final long SONG_INFO_UPDATE_PERIOD = 2000;

	private ICommandController commandService;
	private boolean stopSongInfoPolling = false;
	
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			commandService = ICommandController.Stub.asInterface(service);
			
			// Update the song info now, and start the polling update 
			updateSongInfo(false);
		}
		
		public void onServiceDisconnected(ComponentName name) {
			commandService = null;
		}
	};
	
	private IErrorHandler errorHandler = new IErrorHandler.Stub() {
		public void onServiceError(String title, String message, boolean fatal) throws RemoteException {
			onServiceErrorAction(title, message);
		}
	};
	
    /* This is the main screen, providing the playback controls. */
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.playback);
        
        // Attach the button listeners for playback controls
        View targetButton;
        targetButton = this.findViewById(R.id.pc_power);
        targetButton.setOnClickListener(this);
        targetButton = this.findViewById(R.id.play_pause);
        targetButton.setOnClickListener(this);
        targetButton = this.findViewById(R.id.last_song);
        targetButton.setOnClickListener(this);
        targetButton = this.findViewById(R.id.next_song);
        targetButton.setOnClickListener(this);
        targetButton = this.findViewById(R.id.playlist);
        targetButton.setOnClickListener(this);
        targetButton = this.findViewById(R.id.volume);
        targetButton.setOnClickListener(this);
    }
	
    public void onClick(View v) {
		Command command = null;
		
		try {
			commandService.registerErrorHandler(errorHandler);
			switch (v.getId()) {
			case R.id.pc_power:
				command = commandService.queryForCommand(Command.POWER);
				break;
			case R.id.play_pause: 
				command = commandService.queryForCommand(Command.PLAY);
				break;
			case R.id.last_song:
				command = commandService.queryForCommand(Command.PREV);
				updateSongInfo(true);
				break;
			case R.id.next_song:
				command = commandService.queryForCommand(Command.NEXT);
				updateSongInfo(true);
				break;
			case R.id.playlist:
				new AlertDialog.Builder(Playback.this)
					.setTitle("Not implemented")
					.setMessage("Playlist support is not yet available.")
					.show();
				break;
			case R.id.volume:
				// Start the volume control activity
				Intent intent = new Intent(Playback.this, VolumeControl.class);
				startActivity(intent);
			}
			
			if (command != null) {
				commandService.sendCommand(command);
			}
			commandService.unregisterErrorHandler(errorHandler);
		} catch (RemoteException e) {
			// Failed to send command
			e.printStackTrace();
		}
	}

	private void updateSongInfo(boolean isOverride) {
		if (commandService != null && stopSongInfoPolling == false) {
			GetMediaInfoTask getSongInfoTask = new GetMediaInfoTask();
			getSongInfoTask.registerListener(this);
			getSongInfoTask.execute(commandService);
			
			if (!isOverride) {
				ImageView artwork = (ImageView)findViewById(R.id.album_cover);
				artwork.postDelayed(new Runnable() {
					public void run() {
						updateSongInfo(false);
					}
				}, SONG_INFO_UPDATE_PERIOD);
			}
		}
	}

	protected void onPause() {
		super.onPause();
		stopSongInfoPolling = true;
		if (connection != null) unbindService(connection);
	}

	protected void onResume() {
		super.onResume();
		stopSongInfoPolling = false;
		bindService(new Intent(Playback.this, CommandService.class), connection, Context.BIND_AUTO_CREATE);
	}
	
	public void onServiceErrorAction(String title, String message) {
		onServiceError(title, message);
	}

	public void onServiceError(String title, String message) {
		stopSongInfoPolling = true;
		new AlertDialog.Builder(Playback.this)
			.setTitle(title)
			.setMessage(message)
			.show();
	}

	public void onMediaInfoChanged(MediaInfo info) {
		if (info.title != null && info.artist != null && info.album != null) {
			TextView label;
			label = (TextView)this.findViewById(R.id.song_title);
			label.setText(info.track + " - " + info.title);
			label = (TextView)this.findViewById(R.id.song_artist);
			label.setText(info.artist);
			label = (TextView)this.findViewById(R.id.song_album);
			label.setText(info.album);
			
			// Load the artwork from the cache store
			if (info.artwork != null) {
				ImageView artwork = (ImageView)this.findViewById(R.id.album_cover);
				FileInputStream fis;
				try {
					fis = new FileInputStream(info.artwork);
					Bitmap bitmap = BitmapFactory.decodeStream((InputStream)fis);
					fis.close();
					Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 80, 80, true);
					artwork.setImageBitmap(scaled);
				} catch (Exception e) {
					// Failed to load image from cache store
					e.printStackTrace();
				}
			}
		}
	}
}