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
	
	private static String script = "" +
			"compatiblePrograms=( nautilus kdeinit kded4 pulseaudio trackerd ); " +
			
			"for index in ${compatiblePrograms[@]}; do " +
			"	PID=$(pidof -s ${index}); " +
			"	if [[ \"${PID}\" != \"\" ]]; then break; fi; " +
			"done; " +
			
			"if [[ \"${PID}\" == \"\" ]]; then echo Could not detect active login session; fi; " +
			
			"QUERY_ENVIRON=\"$(tr '\\0' '\\n' < /proc/${PID}/environ | grep 'DBUS_SESSION_BUS_ADDRESS' | cut -d '=' -f 2-)\"; " +
			
			"if [[ \"${QUERY_ENVIRON}\" != \"\" ]]; then " +
			"	export DBUS_SESSION_BUS_ADDRESS=\"${QUERY_ENVIRON}\"; " +
			"	echo success; " +
			"else " +
			"	echo Could not find dbus session ID in user environment; " +
			"fi; " +
			
			"";
}
