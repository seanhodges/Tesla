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

package tesla.app.command.provider.app;

import java.util.HashMap;
import java.util.Map;

import tesla.app.command.Command;
import tesla.app.command.helper.DCopHelper;
import tesla.app.command.provider.IConfigProvider;

public class KaffeineConfig implements IConfigProvider {

	public String getCommand(String key) {
		final String dest = "kaffeine";
		String out = null;
		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
			out = new DCopHelper().compileMethodCall(dest, "KaffeineIface", "pause");
		}
		else if (key.equals(Command.PREV)) {
			out = new DCopHelper().compileMethodCall(dest, "KaffeineIface", "previous");
		}
		else if (key.equals(Command.NEXT)) {
			out = new DCopHelper().compileMethodCall(dest, "KaffeineIface", "next");
		}
		return out;
	}

	public Map<String, String> getSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		return settings;
	}

	public String getLaunchAppCommand() {
		return "pidof kaffeine 1>/dev/null || DISPLAY=:0 kaffeine &>/dev/null & sleep 5";
	}

}
