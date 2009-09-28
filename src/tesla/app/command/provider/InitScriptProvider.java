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


public class InitScriptProvider {
	
	public static String getInitScript() {
		return script;
	}
	
	private static String script = "XDG_ID=\"$(echo $XDG_SESSION_COOKIE | cut -d '-' -f 1)\"; " +
			"QUERY_ENVIRON=\"$(grep DBUS_SESSION_BUS_ADDRESS= ~/.dbus/session-bus/${XDG_ID}-0 | cut -d '=' -f 2,3,4)\"; " +
			"if [[ \"${QUERY_ENVIRON}\" != \"\" ]]; then " +
			"	export DBUS_SESSION_BUS_ADDRESS=\"${QUERY_ENVIRON}\"; " +
			"	echo success; " +
			"else " +
			"	echo Could not find dbus session ID in user environment; " +
			"fi; ";
}
