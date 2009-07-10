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

package tesla.app.ui.task;

import java.util.Map;

import tesla.app.command.Command;
import tesla.app.command.helper.DBusHelper;
import tesla.app.service.business.ICommandController;
import tesla.app.service.business.IErrorHandler;
import android.os.AsyncTask;
import android.os.RemoteException;

public class IsPlayingTask extends AsyncTask<ICommandController, Boolean, Boolean> {

	// If player does not provide a playing status, then assume this value
	private static final boolean DEFAULT_PLAY_MODE = false;

	private OnIsPlayingListener listener = null;
	
	// Error messages need to be passed back to main UI thread
	private String errorTitle = null;
	private String errorMessage = null;
	
	private IErrorHandler errorHandler = new IErrorHandler.Stub() {
		public void onServiceError(String title, String message, boolean fatal) throws RemoteException {
			// Pass the error data back to the main UI thread
			errorTitle = title;
			errorMessage = message;
		}
	};
	
	public interface OnIsPlayingListener {
		void onServiceError(String title, String message);
		void onPlayingChanged(boolean isPlaying);
	}
	
	protected Boolean doInBackground(ICommandController... args) 
	{	
		boolean out = DEFAULT_PLAY_MODE;
		
		Command command = null;
		ICommandController commandService = args[0];
		try {
			commandService.registerErrorHandler(errorHandler);
			command = commandService.queryForCommand(Command.IS_PLAYING);
			
			boolean enabled = false;
			Map<String, String> settings = command.getSettings();
			if (settings.containsKey("ENABLED")) {
				enabled = Boolean.parseBoolean(settings.get("ENABLED"));
			}

			if (enabled) {
				command = commandService.sendQuery(command);
				if (command != null && command.getOutput() != null && command.getOutput() != "") {
					out = new DBusHelper().evaluateOutputAsBoolean(command.getOutput());
				}
			}
		
			commandService.unregisterErrorHandler(errorHandler);
		
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return out;
	}
	
	protected void onPostExecute(Boolean result) {
		if (errorTitle != null && errorMessage != null) {
			if (listener != null) listener.onServiceError(errorTitle, errorMessage);
		}
		else {
			if (listener != null) listener.onPlayingChanged(result);
		}
	}

	public void registerListener(OnIsPlayingListener listener) {
		this.listener = listener;
	}
}