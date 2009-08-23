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

import java.util.HashMap;
import java.util.Map;

import tesla.app.command.Command;
import tesla.app.command.helper.DBusHelper;
import tesla.app.command.helper.DCopHelper;
import tesla.app.command.helper.RhythmDBHelper;
import tesla.app.mediainfo.MediaInfo;
import tesla.app.mediainfo.MediaInfoFactory;
import tesla.app.service.business.ICommandController;
import tesla.app.service.business.IErrorHandler;
import android.os.AsyncTask;
import android.os.RemoteException;

public class GetMediaInfoTask extends AsyncTask<ICommandController, Boolean, MediaInfo> {

	private OnGetMediaInfoListener listener = null;
	
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
	
	public interface OnGetMediaInfoListener {
		void onServiceError(String title, String message);
		void onMediaInfoChanged(MediaInfo info);
	}
	
	protected MediaInfo doInBackground(ICommandController... args) 
	{
		MediaInfo info = new MediaInfo();
		
		// Get the available metadata from the server
		Command command = null;
		ICommandController commandService = args[0];
		try {
			commandService.registerErrorHandler(errorHandler);
			command = commandService.queryForCommand(Command.GET_MEDIA_INFO);
			
			Map<String, String> settings = command.getSettings();
			
			boolean enabled = false;
			if (settings.containsKey("ENABLED")) {
				enabled = Boolean.parseBoolean(settings.get("ENABLED"));
			}
			
			Command.OutputFormat format = Command.OutputFormat.STRING; 
			if (settings.containsKey("FORMAT")) {
				format = Command.OutputFormat.valueOf(settings.get("FORMAT"));
			}
			
			if (enabled) {
				command = commandService.sendQuery(command);
				commandService.unregisterErrorHandler(errorHandler);
			
				// Compile a MediaInfo pod with servers metadata
				if (command != null && command.getOutput() != null && command.getOutput() != "") {
					
					Map<String, String> output;
					switch (format) {
					case DBUS:
						output = new DBusHelper().evaluateOutputAsMap(command.getOutput());
						break;
					case DCOP:
						output = new DCopHelper().evaluateOutputAsMap(command.getOutput());
						break;
					case RHYTHMDB:
						output = new RhythmDBHelper().evaluateMediaInfoAsMap(command.getOutput());
						break;
					default:
						// format defaults to OutputFormat.STRING
						output = new HashMap<String, String>();
						output.put("title", command.getOutput());
					}
					
					info.track = output.get("tracknumber");
					if (info.track == null) info.track = output.get("track-number");
					
					info.title = output.get("title");
					if (info.title == null) info.title = output.get("name");
					
					info.artist = output.get("artist");
					
					info.album = output.get("album");
	
					// Pass the pod to the MediaInfoFactory for processing
					MediaInfoFactory factory = new MediaInfoFactory();
					info = factory.process(info);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return info;
	}
	
	protected void onPostExecute(MediaInfo result) {
		if (errorTitle != null && errorMessage != null) {
			if (listener != null) listener.onServiceError(errorTitle, errorMessage);
		}
		else {
			if (listener != null) listener.onMediaInfoChanged(result);
		}
	}

	public void registerListener(OnGetMediaInfoListener listener) {
		this.listener = listener;
	}
}
