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
import tesla.app.mediainfo.SongInfo;
import tesla.app.service.business.ICommandController;
import tesla.app.service.business.IErrorHandler;
import android.os.AsyncTask;
import android.os.RemoteException;

public class GetSongInfoTask extends AsyncTask<ICommandController, Boolean, SongInfo> {

	private OnGetSongInfoListener listener = null;
	
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
	
	public interface OnGetSongInfoListener {
		void onServiceError(String title, String message);
		void onSongInfoChanged(SongInfo info);
	}
	
	protected SongInfo doInBackground(ICommandController... args) 
	{
		SongInfo info = new SongInfo();
		Command command = null;
		ICommandController commandService = args[0];
		try {
			commandService.registerErrorHandler(errorHandler);
			command = commandService.queryForCommand(Command.GET_SONG_INFO);
			command = commandService.sendQuery(command);
			commandService.unregisterErrorHandler(errorHandler);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		// Parse the result as a level percentage
		if (command != null && command.getOutput() != null && command.getOutput() != "") {
			Map<String, String> output = new DBusHelper().evaluateOutputAsMap(command.getOutput());
			info.track = output.get("tracknumber");
			info.songTitle = output.get("title");
			info.artist = output.get("artist");
			info.album = output.get("album");
		}
		
		return info;
	}
	
	protected void onPostExecute(SongInfo result) {
		if (errorTitle != null && errorMessage != null) {
			if (listener != null) listener.onServiceError(errorTitle, errorMessage);
		}
		else {
			if (listener != null) listener.onSongInfoChanged(result);
		}
	}

	public void registerListener(OnGetSongInfoListener listener) {
		this.listener = listener;
	}
}
