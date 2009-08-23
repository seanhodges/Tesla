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
import tesla.app.command.helper.DCopHelper;
import tesla.app.command.provider.IConfigProvider;

public class AmarokConfig implements IConfigProvider {

	public String getCommand(String key) {
		final String dbusDest = "org.kde.amarok";
		final String dcopDest = "amarok";
		
		List<String> args = new ArrayList<String>();
		String out = null;
		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
			out = new DCopHelper().compileMethodCall(dcopDest, "player", "playPause");
			/*out = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
				"org.freedesktop.MediaPlayer.Pause");*/
		}
		else if (key.equals(Command.PREV)) {
			out = new DCopHelper().compileMethodCall(dcopDest, "player", "prev");
			/*out = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
				"org.freedesktop.MediaPlayer.Prev");*/
		}
		else if (key.equals(Command.NEXT)) {
			out = new DCopHelper().compileMethodCall(dcopDest, "player", "next");
			/*out = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
				"org.freedesktop.MediaPlayer.Next");*/
		}
		else if (key.equals(Command.VOL_CHANGE)) {
			args.add("%i");
			out = new DCopHelper().compileMethodCall(dcopDest, "player", "setVolume", args);
			/*
			args.add(new DBusHelper().evaluateArg("%i"));
			out = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
				"org.freedesktop.MediaPlayer.VolumeSet", args);
			*/
		}
		else if (key.equals(Command.VOL_MUTE)) {
			args.add("%i");
			out = new DCopHelper().compileMethodCall(dcopDest, "player", "setVolume", args);
			/*
			args.add(new DBusHelper().evaluateArg("0"));
			out = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
				"org.freedesktop.MediaPlayer.VolumeSet", args);
			*/
		}
		else if (key.equals(Command.VOL_CURRENT)) {
			out = new DCopHelper().compileMethodCall(dcopDest, "player", "getVolume");
			/*out = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
				"org.freedesktop.MediaPlayer.VolumeGet");*/
		}
		else if (key.equals(Command.GET_MEDIA_INFO)) {
			out = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
				"org.freedesktop.MediaPlayer.GetMetadata");
		}
		return out;
	}

	public Map<String, String> getSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "100.0");
			settings.put("FORMAT", Command.OutputFormat.DCOP.name());
		}
		else if (key.equals(Command.GET_MEDIA_INFO)) {
			settings.put("ENABLED", "false");
			settings.put("FORMAT", Command.OutputFormat.DCOP.name());
		}
		return settings;
	}

}
