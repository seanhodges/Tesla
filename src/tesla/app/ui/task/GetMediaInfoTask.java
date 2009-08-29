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
import tesla.app.mediainfo.MediaInfo;
import tesla.app.mediainfo.MediaInfoFactory;
import tesla.app.service.business.ICommandController;
import tesla.app.service.business.IErrorHandler;
import android.os.AsyncTask;
import android.os.RemoteException;

public class GetMediaInfoTask extends AsyncTask<ICommandController, Boolean, MediaInfo> {

	private OnGetMediaInfoListener listener = null;
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
	
	public interface OnGetMediaInfoListener {
		void onServiceError(Class<? extends Object> invoker, String title, String message, Command command);
		void onMediaInfoChanged(MediaInfo info);
	}
	
	protected MediaInfo doInBackground(ICommandController... args) 
	{
		MediaInfo info = new MediaInfo();
		
		// Get the available metadata from the server
		ICommandController commandService = args[0];
		try {
			commandService.registerErrorHandler(errorHandler);
			command = commandService.queryForCommand(Command.GET_MEDIA_INFO, false);
			
			Map<String, String> settings = command.getSettings();
			
			boolean enabled = false;
			if (settings.containsKey("ENABLED")) {
				enabled = Boolean.parseBoolean(settings.get("ENABLED"));
			}
			
			if (enabled) {
				command = commandService.sendQuery(command);
			
				// Compile a MediaInfo pod with servers metadata
				if (command != null && command.getOutput() != null && command.getOutput() != "") {
					ICommandHelper helper = CommandHelperFactory.getHelperForCommand(command);
					
					Map<String, String> output;
					output = helper.evaluateOutputAsMap(command.getOutput());
					
					if (output != null) {
						info.track = output.get("tracknumber");
						if (info.track == null) info.track = output.get("track-number");
						
						// Title can be taken from several metadata fields
						info.title = output.get("title");
						if (info.title == null) info.title = output.get("name");
						if (info.title == null) info.title = output.get("location");
						if (info.title == null) info.title = output.get("URI");
						if (info.title == null) info.title = output.get("uri");
						
						if (info.title != null && (info.title.startsWith("file:/") || info.title.startsWith("/"))) info.title = stripTitleFromUrl(info.title);
						
						info.artist = output.get("artist");
						
						info.album = output.get("album");
						
						// Pass the pod to the MediaInfoFactory for processing
						MediaInfoFactory factory = new MediaInfoFactory();
						info = factory.process(info);
					}
				}
			}
			
			commandService.unregisterErrorHandler(errorHandler);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return info;
	}
	
	private String stripTitleFromUrl(String uri) {
		return uri.substring(uri.lastIndexOf("/") + 1);
	}

	protected void onPostExecute(MediaInfo result) {
		if (errorTitle != null && errorMessage != null) {
			if (listener != null) listener.onServiceError(getClass(), errorTitle, errorMessage, command);
		}
		else {
			if (listener != null) listener.onMediaInfoChanged(result);
		}
	}

	public void registerListener(OnGetMediaInfoListener listener) {
		this.listener = listener;
	}
}
