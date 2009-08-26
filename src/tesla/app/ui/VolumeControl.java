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

import tesla.app.R;
import tesla.app.command.Command;
import tesla.app.ui.task.GetVolumeLevelTask;
import tesla.app.ui.widget.VolumeSlider;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.Toast;

public class VolumeControl extends AbstractTeslaActivity implements VolumeSlider.OnVolumeLevelChangeListener, GetVolumeLevelTask.OnGetVolumeLevelListener {
	
	private VolumeSlider volumeSlider;
	
    /* This is the volume control. */
	
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.volume_control);
        
        volumeSlider = (VolumeSlider)this.findViewById(R.id.volume);
        volumeSlider.setOnVolumeLevelChangeListener(this);
    }

	protected void onTeslaServiceConnected() {
		setInitialVolume();
	}
	
	protected void onTeslaServiceDisconnected() {
		// Do nothing
	}
	
	private void updateVolume(VolumeSlider volumeSlider, float level) {
		Command command = null;
		
		try {
			commandService.registerErrorHandler(errorHandler);
			
	    	if (level == 0) {
	    		command = commandService.queryForCommand(Command.VOL_MUTE); 
	    	}
	    	else {
				command = commandService.queryForCommand(Command.VOL_CHANGE);
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
			commandService.sendCommand(command);
			
			commandService.unregisterErrorHandler(errorHandler);
		} catch (RemoteException e) {
			// Failed to send command
			e.printStackTrace();
		}
    }
	
	public void onLevelChanged(VolumeSlider volumeSlider, float level) {
		updateVolume(volumeSlider, level);
	}
	
	private void setInitialVolume() {
        volumeSlider.setLevel(0.0f);
		GetVolumeLevelTask getVolumeTask = new GetVolumeLevelTask(commandService);
		getVolumeTask.registerListener(VolumeControl.this);
		getVolumeTask.execute();
	}

	public void onGetVolumeExtents(float min, float max) {
		volumeSlider.setMinVolume(min);
		volumeSlider.setMaxVolume(max);
	}

	public void onGetVolumeComplete(Float result) {
		volumeSlider.setLevel(result);
		volumeSlider.refresh();
	}

	public void onServiceError(String title, String message) {
		showErrorMessage(title, message);
	}

	public void onChangeFinished(VolumeSlider volumeSlider) {
		String message = getResources().getText(R.string.volume_update) + " " + volumeSlider.getLevelPercent() + "%";
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
		// Close the activity when the user stops touching the slider
		finish();
	}
}
