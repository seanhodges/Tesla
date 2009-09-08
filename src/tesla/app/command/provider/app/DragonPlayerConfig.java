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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tesla.app.command.Command;
import tesla.app.command.helper.DBusHelper;
import tesla.app.command.provider.IConfigProvider;

public class DragonPlayerConfig implements IConfigProvider {

	public String getCommand(String key) {
		final String dest = "org.mpris.dragonplayer-$(pidof dragon)";
		List<String> args = new ArrayList<String>();
		String out = null;
		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.PlayPause");
		}
		else if (key.equals(Command.PREV)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.Prev");
		}
		else if (key.equals(Command.NEXT)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.Next");
		}
		else if (key.equals(Command.VOL_CHANGE)) {
			args.add(new DBusHelper().evaluateArg("%i"));
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.VolumeSet", args);
		}
		else if (key.equals(Command.VOL_MUTE)) {
			args.add(new DBusHelper().evaluateArg("0"));
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.VolumeSet", args);
		}
		else if (key.equals(Command.VOL_CURRENT)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.VolumeGet");
		}
		else if (key.equals(Command.GET_MEDIA_INFO)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.GetMetadata");
		}
		else if (key.equals(Command.IS_PLAYING)) {
			String dbusCommand = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.GetStatus", false);
			out = "if [[ \"$(" + dbusCommand + " | sed -n '3p')\" == \"      int32 0\" ]]; then echo \"PLAYING\"; fi";
		}
		else if (key.equals(Command.GET_MEDIA_POSITION)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.PositionGet");
		}
		else if (key.equals(Command.SET_MEDIA_POSITION)) {
			args.add(new DBusHelper().evaluateArg("%i"));
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.PositionSet", args);
		}
		return out;
	}

	public Map<String, String> getSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "100.0");
		}
		else if (key.equals(Command.IS_PLAYING)) {
			settings.put("ENABLED", "true");
		}
		return settings;
	}

	public String getLaunchAppCommand() {
		return "pidof dragon 1>/dev/null || DISPLAY=:0 dragon &>/dev/null & sleep 5 && echo success";
	}

}
