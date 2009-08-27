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

package tesla.app.command.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tesla.app.command.Command;
import tesla.app.command.helper.DBusHelper;

public class GlobalConfigProvider implements IConfigProvider {

	public String getCommand(String key) {
		String out = null;
		
		if (key.equals(Command.POWER)) {
			out = powerCommand();
		}
		
		return out;
	}

	private String powerCommand() {
		// KDE-compatible command
		List<String> args = new ArrayList<String>();
		// Confirmation (-1 : user-defined, 0 : immediate, 1 : prompt user)
		args.add(new DBusHelper().evaluateArg("-1"));
		// Logout type (-1 : previous, 0 : logout, 1 : reboot, 2 : halt)
		args.add(new DBusHelper().evaluateArg("2")); 
		// Session handling (-1 : previous, 0 : wait for other sessions, 1 : cancel if other sessions active, 2 : force, 3 : prompt user)
		args.add(new DBusHelper().evaluateArg("3"));
		String kdeCommand = new DBusHelper().compileMethodCall("org.kde.ksmserver", "/KSMServer", 
				"org.kde.KSMServerInterface.logout", args);
		
		// Gnome-compatible command
		String gnomeCommand = new DBusHelper().compileMethodCall("org.gnome.SessionManager", "/org/gnome/SessionManager", 
				"org.gnome.SessionManager.Shutdown");
		
		return "pidof ksmserver && " + kdeCommand + " || " + gnomeCommand;
	}

	public Map<String, String> getSettings(String key) {
		return null;
	}

	public String getLaunchAppCommand() {
		return null;
	}

}
