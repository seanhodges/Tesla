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

import java.util.HashMap;
import java.util.Map;

import tesla.app.command.Command;

public class FallbackConfigProvider implements IConfigProvider {

	public String getCommand(String key) {
		String out = null;
		if (key.equals(Command.VOL_CURRENT)) {
			out = "(amixer get \"Master\" 2>/dev/null " +
					"|| amixer get \"Front\" 2>/dev/null " +
					"|| amixer get \"PCM\"" +
					") | grep -m 1 \"\\[on\\]\" | cut -d \"[\" -f 2 | sed -e \"s/[^0-9]//g\"";
		}
		else if (key.equals(Command.VOL_CHANGE)) {
			out = "amixer set \"Master\" %i% unmute &>/dev/null || amixer set \"Front\" %i% unmute &>/dev/null || amixer set \"PCM\" %i% unmute 1>/dev/null";
		}
		else if (key.equals(Command.VOL_MUTE)) {
			out = "amixer set \"Master\" 0% unmute &>/dev/null || amixer set \"Front\" 0% unmute &>/dev/null || amixer set \"PCM\" 0% unmute 1>/dev/null";
		}
		else {
			// No command was found
			out = "";
		}
		
		return out;
	}

	public Map<String, String> getSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "100.0");
		}
		return settings;
	}

	public String getLaunchAppCommand() {
		return "";
	}

}
