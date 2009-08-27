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
import tesla.app.command.provider.IConfigProvider;

public class TotemConfig implements IConfigProvider {

	public String getCommand(String key) {
		String out = null;
		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
			out = "DISPLAY=:0 totem --play-pause";
		}
		else if (key.equals(Command.PREV)) {
			out = "DISPLAY=:0 totem --previous";
		}
		else if (key.equals(Command.NEXT)) {
			out = "DISPLAY=:0 totem --next";
		}
		
		return out;
	}

	public Map<String, String> getSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		return settings;
	}

}
