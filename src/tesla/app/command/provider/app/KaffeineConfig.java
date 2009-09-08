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
		else if (key.equals(Command.GET_MEDIA_INFO)) {
			out = buildMediaInfoDCopMethodCallSet(dest);
		}
		else if (key.equals(Command.IS_PLAYING)) {
			// This is currently broken, isPlaying does not return false if player is paused
			//out = new DCopHelper().compileMethodCall(dest, "KaffeineIface", "isPlaying");
		}
		return out;
	}

	public Map<String, String> getSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.GET_MEDIA_INFO)) {
			settings.put("ENABLED", "true");
		}
		return settings;
	}

	private String buildMediaInfoDCopMethodCallSet(String dcopDest) {
		// DCOP has no concept of a hashmap, so we build one here
		StringBuilder builder = new StringBuilder();
		builder.append("echo \"[dcop]\";");
		builder.append("echo -n \"tracknumber:\";");
		builder.append(new DCopHelper().compileMethodCall(dcopDest, "KaffeineIface", "track", false));
		builder.append(";echo -n \"title:\";");
		builder.append(new DCopHelper().compileMethodCall(dcopDest, "KaffeineIface", "title", false));
		builder.append(";echo -n \"artist:\";");
		builder.append(new DCopHelper().compileMethodCall(dcopDest, "KaffeineIface", "artist", false));
		builder.append(";echo -n \"album:\";");
		builder.append(new DCopHelper().compileMethodCall(dcopDest, "KaffeineIface", "album", false));
		return builder.toString();
	}

	public String getLaunchAppCommand() {
		return "pidof kaffeine 1>/dev/null || DISPLAY=:0 kaffeine &>/dev/null & sleep 5 && echo success";
	}

}
