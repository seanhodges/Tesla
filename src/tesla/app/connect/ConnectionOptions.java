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

package tesla.app.connect;

import android.content.Context;
import android.content.SharedPreferences;

public class ConnectionOptions {
	
	private static final String PREFS_NAME = "ConnectionSettings";
	
	private Context owner;
	
	public String hostname = null;
	public int port = 0;
	public String username = null;
	public String password = null;
	public String appSelection = "";
	
	public ConnectionOptions(Context owner) {
		this.owner = owner;
		reloadSettings();
	}
	
	public void reloadSettings() {
		// This is a simple load/save mechanism from a simple file
		// TODO: replace/extend with a profile manager
		SharedPreferences settings = owner.getSharedPreferences(PREFS_NAME, 0);
		hostname = settings.getString("hostname", "192.168.0.1");
		port = settings.getInt("port", 22);
		username= settings.getString("username", "User");
		password = settings.getString("password", "");
		appSelection = settings.getString("appSelection", "");
	}
	
	public void saveSettings() {
		SharedPreferences settings = owner.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("hostname", hostname);
		editor.putInt("port", port);
		editor.putString("username", username);
		editor.putString("password", password);
		editor.putString("appSelection", appSelection);
		editor.commit();
	}
}
