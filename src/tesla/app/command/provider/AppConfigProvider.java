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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tesla.app.command.Command;
import tesla.app.command.helper.DBusHelper;

public class AppConfigProvider {

	public static final String APP_RHYTHMBOX = "rhythmbox";
	public static final String APP_AMAROK = "amarok";
	public static final String APP_VLC = "vlc";
	
	public String appName = "amarok";
	
	public AppConfigProvider(String appName) {
		this.appName = appName;
	}
	
	public String getCommand(String key) throws Exception {
		
		// These commands will be extracted from
		// a database of application configurations
		
		String out = null;
		
		if (appName.equals(APP_RHYTHMBOX)) {
			out = rhythmBoxCommand(key);
		}
		else if (appName.equals(APP_AMAROK)) {
			out = amarokCommand(key);
		}
		else if (appName.equals(APP_VLC)) {
			out = vlcCommand(key);
		}
		
		return out;
	}
	
	public Map<String, String> getSettings(String key) {
		Map<String, String> settings = null;
		
		if (appName.equals(APP_RHYTHMBOX)) {
			settings = rhythmboxSettings(key);
		}
		else if (appName.equals(APP_AMAROK)) {
			settings = amarokSettings(key);
		}
		else if (appName.equals(APP_VLC)) {
			settings = vlcSettings(key);
		}
		
		return settings;
	}
	
	private Map<String, String> rhythmboxSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "1.0");
		}
		return settings;
	}
	
	private Map<String, String> amarokSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "100.0");
		}
		return settings;
	}
	
	private Map<String, String> vlcSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "25.0");
		}
		return settings;
	}
	
	private String rhythmBoxCommand(String key) throws Exception {
		final String dest = "org.gnome.Rhythmbox";
		List<String> args = new ArrayList<String>();
		String out = "";
		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
			args.add(new DBusHelper().evaluateArg("false"));
			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
				"org.gnome.Rhythmbox.Player.playPause", args);
		}
		else if (key.equals(Command.PREV)) {
			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
				"org.gnome.Rhythmbox.Player.previous");
		}
		else if (key.equals(Command.NEXT)) {
			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
				"org.gnome.Rhythmbox.Player.next");
		}
		else if (key.equals(Command.VOL_CHANGE)) {
			args.add(new DBusHelper().evaluateArg("%f"));
			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
				"org.gnome.Rhythmbox.Player.setVolume", args);
		}
		else if (key.equals(Command.VOL_MUTE)) {
			args.add(new DBusHelper().evaluateArg("0.0"));
			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
				"org.gnome.Rhythmbox.Player.setVolume", args);
		}
		else if (key.equals(Command.VOL_CURRENT)) {
			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
				"org.gnome.Rhythmbox.Player.getVolume");
		}
		else {
			throw new Exception("Command not implemented");
		}
		
		return out;
	}

	private String amarokCommand(String key) throws Exception {
		final String dest = "org.kde.amarok";
		List<String> args = new ArrayList<String>();
		String out = "";
		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.Pause");
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
		else {
			throw new Exception("Command not implemented");
		}
		return out;
	}

	private String vlcCommand(String key) throws Exception {
		final String dest = "org.mpris.vlc";
		List<String> args = new ArrayList<String>();
		String out = "";
		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.Pause");
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
		else {
			throw new Exception("Command not implemented");
		}
		return out;
	}
}
