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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import tesla.app.R;
import tesla.app.command.Command;
import tesla.app.command.provider.AppConfigProvider;
import tesla.app.mediainfo.MediaInfo;
import tesla.app.service.CommandService;
import tesla.app.service.connect.ConnectionOptions;
import tesla.app.ui.task.PlaybackUpdateTask;
import tesla.app.ui.task.PlaybackUpdateTask.PlaybackUpdateListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Playback extends AbstractTeslaActivity 
		implements 
			OnClickListener, 
			PlaybackUpdateListener,
			OnSeekBarChangeListener {

	private static final long UI_UPDATE_PERIOD = 2000;
	private static final int APP_SELECTOR_RESULT = 1;
	
	// Options menu item ID's
	private static final int TARGET_APP_SELECTOR_ITEM = 500;
	private static final int PREFERENCES_MENU_ITEM = 501;
	
	private boolean stopSongInfoPolling = false;
	private boolean appReportingIfPlaying = false;
	private boolean seekBarEnabled = false;
	private String versionString;
	
	private Handler updateUIHandler = new Handler();
	private Runnable updateUIRunnable = new Runnable() {
		public void run() {
			updateUI();
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
        targetButton.setSelected(true);
        targetButton = this.findViewById(R.id.last_song);
        targetButton.setOnClickListener(this);
        targetButton = this.findViewById(R.id.next_song);
        targetButton.setOnClickListener(this);
        targetButton = this.findViewById(R.id.playlist);
        targetButton.setOnClickListener(this);
        targetButton = this.findViewById(R.id.volume);
        targetButton.setOnClickListener(this);
        
        SeekBar seekBar = (SeekBar)findViewById(R.id.media_progress);
        seekBar.setOnSeekBarChangeListener(this);
		
        // Get the version string
        PackageInfo info;
		try {
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionString = "version " + info.versionName;
		} catch (NameNotFoundException e) {
			// Could not get version info
			versionString = "";
		}
		setLabelTextIfChanged((TextView)this.findViewById(R.id.song_album), versionString);
        
        setAppIcon();
    }

	protected void onTeslaServiceConnected() {
        try {
			commandService.registerErrorHandler(errorHandler);
			
			// If the application is reporting whether it is playing, we don't want to toggle the play button manually			
			Command command = commandService.queryForCommand(Command.IS_PLAYING, false);
			Map<String, String> settings = command.getSettings();
			if (settings.containsKey("ENABLED")) {
				appReportingIfPlaying = Boolean.parseBoolean(settings.get("ENABLED"));
			}
			
			commandService.unregisterErrorHandler(errorHandler);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		showHideSeekBar();
		
		// Update the song info now, and start the polling update 
		updateUI();
	}
	
	private void showHideSeekBar() {
		// Turn on the seek bar if the player can support it
		try {
			commandService.registerErrorHandler(errorHandler);
			Command command = commandService.queryForCommand(Command.GET_MEDIA_POSITION, false);
			Map<String, String> settings = command.getSettings();
			if (settings.containsKey("ENABLED")) {
				seekBarEnabled = Boolean.parseBoolean(settings.get("ENABLED"));
			}
			
			// Hide the seek bar if the player does not support it
			int visible = View.VISIBLE;
			if (!seekBarEnabled) {
				visible = View.INVISIBLE;
			}
			SeekBar seekBar = (SeekBar)findViewById(R.id.media_progress);
	        seekBar.setVisibility(visible);
	        
	        // If the seek bar is hidden, move the media info 
	        // panel down a bit to accommodate the missing space
	        int panelTop = 10;
	        if (!seekBarEnabled) {
	        	panelTop = 30;
	        }
	    	View mediaInfoPanel = (View)findViewById(R.id.media_info_panel);
	    	mediaInfoPanel.setPadding(-1, panelTop, -1, -1);

			commandService.unregisterErrorHandler(errorHandler);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	protected void onTeslaServiceDisconnected() {
		// Do nothing
	}
	
    public void onClick(View v) {
		Command command = null;
		
		try {
			commandService.registerErrorHandler(errorHandler);
			switch (v.getId()) {
			case R.id.pc_power:
				confirmPowerButton();
				break;
			case R.id.play_pause: 
				togglePlayPauseButtonMode();
				command = commandService.queryForCommand(Command.PLAY, false);
				break;
			case R.id.last_song:
				command = commandService.queryForCommand(Command.PREV, false);
				updateUIHandler.removeCallbacks(updateUIRunnable);
				updateUIHandler.postDelayed(updateUIRunnable, UI_UPDATE_PERIOD);
				break;
			case R.id.next_song:
				command = commandService.queryForCommand(Command.NEXT, false);
				updateUIHandler.removeCallbacks(updateUIRunnable);
				updateUIHandler.postDelayed(updateUIRunnable, UI_UPDATE_PERIOD);
				break;
			case R.id.playlist:
				new AlertDialog.Builder(Playback.this)
					.setTitle("Not implemented")
					.setMessage("Playlist support is not yet available.")
					.setNegativeButton(getResources().getText(R.string.btn_close), 
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}
					)
					.show();
				break;
			case R.id.volume:
				// Start the volume control activity
				Intent intent = new Intent(Playback.this, VolumeControl.class);
				startActivity(intent);
				break;
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

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// Do nothing
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// Do nothing
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		Command command = null;
		
		try {
			commandService.registerErrorHandler(errorHandler);
			switch (seekBar.getId()) {
			case R.id.media_progress:
				if (seekBarEnabled) {
					command = commandService.queryForCommand(Command.SET_MEDIA_POSITION, false);
					command.addArg(new Integer(seekBar.getProgress()));
				}
				break;
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

	private void confirmPowerButton() {
		// Ask the user before executing this action
		new AlertDialog.Builder(Playback.this)
		.setTitle(getResources().getText(R.string.shut_down_check_title))
		.setMessage(getResources().getText(R.string.shut_down_check_body))
		.setNegativeButton(getResources().getText(R.string.btn_close), 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}
		)
		.setPositiveButton(getResources().getText(R.string.btn_shutdown), 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					try {
						commandService.registerErrorHandler(errorHandler);
						Command command = commandService.queryForCommand(Command.POWER, false);
						if (command != null) {
							commandService.sendCommand(command);
						}
						commandService.unregisterErrorHandler(errorHandler);
					} catch (RemoteException e) {
						// Failed to send command
						e.printStackTrace();
					}
					// Finish the service/activity regardless of the outcome
					String message = (String)getResources().getText(R.string.shutting_down);
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
					stopService(new Intent(Playback.this, CommandService.class));
					finish();
				}
			}
		)
		.show();
	}

	private void togglePlayPauseButtonMode() {
		if (!appReportingIfPlaying) {
			ImageButton button = (ImageButton)this.findViewById(R.id.play_pause);
			button.setSelected(!button.isSelected());
			button.refreshDrawableState();
		}
	}

	private void updateUI() {
		updateUIHandler.removeCallbacks(updateUIRunnable);
		
		if (commandService != null && stopSongInfoPolling == false) {
			
			// Run the update task
			try {
				PlaybackUpdateTask playbackUpdateTask = new PlaybackUpdateTask();
				playbackUpdateTask.registerListener(this);
				playbackUpdateTask.execute(commandService);
			}
			catch (RejectedExecutionException e) {
				// Ignore failed executions
			}
		}
		
		updateUIHandler.postDelayed(updateUIRunnable, UI_UPDATE_PERIOD);
	}

	protected void onPause() {
		super.onPause();
		stopSongInfoPolling = true;
	}

	protected void onResume() {
		super.onResume();
		stopSongInfoPolling = false;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, TARGET_APP_SELECTOR_ITEM, 0, R.string.menu_application_change);
		menu.add(0, PREFERENCES_MENU_ITEM, 1, R.string.menu_preferences_launch);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case TARGET_APP_SELECTOR_ITEM:
			startActivityForResult(new Intent(Playback.this, AppSelector.class), APP_SELECTOR_RESULT);
			return true;
		case PREFERENCES_MENU_ITEM:
			startActivity(new Intent(this, PlaybackPreferences.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case APP_SELECTOR_RESULT:
				// Target media player has been changed
				try {
					commandService.reloadCommandFactory();
					setAppIcon();
					showHideSeekBar();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}

	private void setAppIcon() {
		ConnectionOptions config = new ConnectionOptions(this);
		Map<String, String> appInfo = AppConfigProvider.findAppMatchingName(config.appSelection);
		if (appInfo.containsKey("icon")) {
			int iconRef = Integer.valueOf(appInfo.get("icon"));
			if (iconRef > 0) {
				ImageView appIcon = (ImageView)findViewById(R.id.app_icon);
				appIcon.setImageResource(iconRef);
			}
		}
	}

	public void onServiceError(Class<? extends Object> invoker, String title, String message, Command command) {
		stopSongInfoPolling = true;
		showErrorMessage(invoker, title, message, command);
	}

	public void onMediaInfoChanged(MediaInfo info) {
		TextView labelTitle = (TextView)this.findViewById(R.id.song_title);
		TextView labelArtist = (TextView)this.findViewById(R.id.song_artist);
		TextView labelAlbum = (TextView)this.findViewById(R.id.song_album);
		ImageView artwork = (ImageView)this.findViewById(R.id.album_cover);
		
		if (info.title != null && info.artist != null && info.album != null) {
			// All media info is available
			String newTitle = info.title;
			if (info.track != null) {
				newTitle = info.track + " - " + newTitle; 
			}
				
			setLabelTextIfChanged(labelTitle, newTitle);
			setLabelTextIfChanged(labelArtist, info.artist);
			setLabelTextIfChanged(labelAlbum, info.album);
			
			// Load the artwork from the cache store
			boolean failed = false;
			if (info.artwork != null && new File(info.artwork).exists()) {
				FileInputStream fis;
				try {
					fis = new FileInputStream(info.artwork);
					Bitmap bitmap = BitmapFactory.decodeStream((InputStream)fis);
					fis.close();
					Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 80, 80, true);
					artwork.setImageBitmap(scaled);
				} catch (Exception e) {
					// Failed to load image from cache store
					failed = true;
				}
			}
			else failed = true;
			if (failed == true) {
				// Revert to the generic CD cover image
				artwork.setImageResource(R.drawable.album_cover);
			}
		}
		else if (info.title != null) {
			// Only the title is available
			setLabelTextIfChanged(labelTitle, "");
			setLabelTextIfChanged(labelArtist, info.title);
			setLabelTextIfChanged(labelAlbum, "");
			artwork.setImageResource(R.drawable.album_cover);
		}
		else {
			// Revert to the generic media info text
			setLabelTextIfChanged(labelTitle, getResources().getText(R.string.info_title).toString());
			setLabelTextIfChanged(labelArtist, getResources().getText(R.string.info_artist).toString());
			setLabelTextIfChanged(labelAlbum, versionString);
			artwork.setImageResource(R.drawable.album_cover);
		}
	}
	
	private void setLabelTextIfChanged(TextView label, String newString) {
		if (!label.getText().equals(newString)) {
			label.setText(newString);
		}
	}

	public void onPlayingChanged(boolean isPlaying) {
        ImageButton playPauseButton = (ImageButton)this.findViewById(R.id.play_pause);
        playPauseButton.setSelected(isPlaying);
	}
	
	protected void onPhoneIsBusy() {
		togglePlayPauseButtonMode();
	}
	
	protected void onPhoneIsIdle() {
		togglePlayPauseButtonMode();
	}

	public void onMediaProgressChanged(int currentProgress, int mediaLength) {
		SeekBar seekBar = (SeekBar)findViewById(R.id.media_progress);
		seekBar.setMax(mediaLength);
		seekBar.setProgress(currentProgress);
	}
}