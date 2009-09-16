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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tesla.app.R;
import tesla.app.command.Command;
import tesla.app.command.helper.CommandHelperFactory;
import tesla.app.command.helper.ICommandHelper;
import tesla.app.command.provider.AppConfigProvider;
import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Playlist extends AbstractTeslaListActivity {

	private List<Map<String, String>> providerList;
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
        setContentView(R.layout.playlist);
	}

	protected void onTeslaServiceConnected() {
		populatePlaylist();
	}
	
	private void populatePlaylist() {
		// Get the playlist titles from the server
		List<String> data = null;
		try {
			commandService.registerErrorHandler(errorHandler);
			Command command = commandService.queryForCommand(Command.GET_PLAYLIST, false);
			command = commandService.sendQuery(command);
			commandService.unregisterErrorHandler(errorHandler);
			if (command != null && command.getOutput() != null && command.getOutput() != "") {
				ICommandHelper helper = CommandHelperFactory.getHelperForCommand(command);
				data = helper.evaluateOutputAsList(command.getOutput());
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		// Build a providerList containing the titles and corresponding icon
		providerList = new ArrayList<Map<String,String>>();
		for (String title : data) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("icon", String.valueOf(R.drawable.note));
			item.put("title", title);
			providerList.add(item);
		}
		
		// Set up the list adapter
		ListAdapter providerSelector = new SimpleAdapter(
        		this, providerList, R.layout.playlist_entry, 
        		new String[] { "icon", "title" }, 
        		new int[] { R.id.playlist_entry_icon, R.id.playlist_entry_title });
        setListAdapter(providerSelector);
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		changePlayingItem(position);
	}
	
	private void changePlayingItem(int itemIndex) {
		try {
			commandService.registerErrorHandler(errorHandler);
			Command command = commandService.queryForCommand(Command.SET_PLAYLIST_SELECTION, false);
			command.addArg(itemIndex);
			commandService.sendCommand(command);
			commandService.unregisterErrorHandler(errorHandler);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		setResult(Activity.RESULT_OK);
		finish();
	}
	
	private void refreshPlayingItem() {
		// TODO: Query the server for the currently playing item, and set the icon against the listview item
	}
	
	protected void onPhoneIsBusy() {
		// Do nothing
	}
	
	protected void onPhoneIsIdle() {
		// Do nothing
	}
	
	protected void onTeslaServiceDisconnected() {
		// Do nothing
	}
}
