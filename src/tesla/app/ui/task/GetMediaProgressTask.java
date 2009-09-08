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
import tesla.app.command.helper.CommandHelperFactory;
import tesla.app.command.helper.ICommandHelper;
import tesla.app.service.business.ICommandController;
import tesla.app.service.business.IErrorHandler;
import android.os.AsyncTask;
import android.os.RemoteException;

public class GetMediaProgressTask extends AsyncTask<ICommandController, Boolean, GetMediaProgressTask.ProgressData> {

	// If player does not provide a valid position, then assume this value
	private static final int DEFAULT_POSITION = 0;
	private static final int DEFAULT_MAX = 0;

	private OnMediaProgressListener listener = null;
	private Command command;
	
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
	
	public interface OnMediaProgressListener {
		void onServiceError(Class<? extends Object> invoker, String title, String message, Command command);
		void onMediaProgressChanged(int currentProgress, int mediaLength);
	}
	
	// This should be private, but Java 5 does not allow it
	class ProgressData {
		public int current = DEFAULT_POSITION;
		public int max = DEFAULT_MAX;
	}
	
	protected ProgressData doInBackground(ICommandController... args) {
		ProgressData out = new ProgressData();
		
		ICommandController commandService = args[0];
		try {
			commandService.registerErrorHandler(errorHandler);
			command = commandService.queryForCommand(Command.GET_MEDIA_POSITION, false);
			
			boolean enabled = false;
			Map<String, String> settings = command.getSettings();
			if (settings.containsKey("ENABLED")) {
				enabled = Boolean.parseBoolean(settings.get("ENABLED"));
			}

			if (enabled) {
				boolean success = false;
				
				command = commandService.sendQuery(command);
				if (command != null && command.getOutput() != null && command.getOutput() != "") {
					
					// Get the current position
					ICommandHelper helper = CommandHelperFactory.getHelperForCommand(command);
					
					String data = helper.evaluateOutputAsString(command.getOutput());
					if (data != null && data.length() > 0) {
						try {
							out.current = Integer.parseInt(data.trim());
							success = true;
						}
						catch (NumberFormatException e) {
							// Value returned was not a valid integer
						}
					}
					
					// Get the length of the media being played
					command = commandService.queryForCommand(Command.GET_MEDIA_LENGTH, false);
					command = commandService.sendQuery(command);
					if (success == true && command != null && command.getOutput() != null && command.getOutput() != "") {
						// Get the current position
						helper = CommandHelperFactory.getHelperForCommand(command);
						
						data = helper.evaluateOutputAsString(command.getOutput());
						if (data != null && data.length() > 0) {
							try {
								out.max = Integer.parseInt(data.trim());
							}
							catch (NumberFormatException e) {
								// Value returned was not a valid integer
							}
						}
					}
				}
			}
		
			commandService.unregisterErrorHandler(errorHandler);
		
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		// Return a POD containing all the data collected
		return out;
	}
	
	protected void onPostExecute(ProgressData result) {
		if (errorTitle != null && errorMessage != null) {
			if (listener != null) listener.onServiceError(getClass(), errorTitle, errorMessage, command);
		}
		else {
			if (listener != null) listener.onMediaProgressChanged(result.current, result.max);
		}
	}

	public void registerListener(OnMediaProgressListener listener) {
		this.listener = listener;
	}
}
