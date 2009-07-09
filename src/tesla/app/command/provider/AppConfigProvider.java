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

import tesla.app.R;
import tesla.app.command.Command;
import tesla.app.command.helper.DBusHelper;
import tesla.app.command.helper.RhythmDBHelper;
import tesla.app.mediainfo.MediaInfo;

public class AppConfigProvider implements IConfigProvider {

	public static final String APP_RHYTHMBOX = "rhythmbox";
	public static final String APP_AMAROK = "amarok";
	public static final String APP_VLC = "vlc";
	public static final String APP_TOTEM = "totem";
	public static final String APP_DRAGONPLAYER = "dragon player";
	
	public String appName = "amarok";
	
	public static final List<Map<String, String>> getAppDirectory() {
		
		ArrayList<Map<String, String>> providerList = new ArrayList<Map<String,String>>();
		Map<String, String> entry;
		
        entry = new HashMap<String, String>();
        entry.put("icon", String.valueOf(R.drawable.app_icon_amarok));
        entry.put("name", "amaroK");
        entry.put("ref", AppConfigProvider.APP_AMAROK);
        providerList.add(entry);
        
        entry = new HashMap<String, String>();
        entry.put("icon", String.valueOf(R.drawable.app_icon_dragonplayer));
        entry.put("name", "Dragon Player");
        entry.put("ref", AppConfigProvider.APP_DRAGONPLAYER);
        providerList.add(entry);
        
        entry = new HashMap<String, String>();
        entry.put("icon", String.valueOf(R.drawable.app_icon_rhythmbox));
        entry.put("name", "Rhythmbox");
        entry.put("ref", AppConfigProvider.APP_RHYTHMBOX);
        providerList.add(entry);
        
        entry = new HashMap<String, String>();
        entry.put("icon", String.valueOf(R.drawable.app_icon_totem));
        entry.put("name", "Totem");
        entry.put("ref", AppConfigProvider.APP_TOTEM);
        providerList.add(entry);
        
        entry = new HashMap<String, String>();
        entry.put("icon", String.valueOf(R.drawable.app_icon_vlc));
        entry.put("name", "VLC");
        entry.put("ref", AppConfigProvider.APP_VLC);
        providerList.add(entry);
        
        return providerList;
	}
	
	public AppConfigProvider(String appName) {
		this.appName = appName;
	}
	
	public String getCommand(String key) {
		
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
		else if (appName.equals(APP_TOTEM)) {
			out = totemCommand(key);
		}
		else if (appName.equals(APP_DRAGONPLAYER)) {
			out = dragonPlayerCommand(key);
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
		else if (appName.equals(APP_DRAGONPLAYER)) {
			settings = dragonPlayerSettings(key);
		}
		
		return settings;
	}
	
	Map<String, String> rhythmboxSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "1.0");
		}
		else if (key.equals(Command.GET_MEDIA_INFO)) {
			settings.put("ENABLED", "true");
			settings.put("FORMAT", MediaInfo.FORMAT_RHYTHMDB);
		}
		else if (key.equals(Command.IS_PLAYING)) {
			settings.put("ENABLED", "true");
		}
		return settings;
	}
	
	Map<String, String> amarokSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "100.0");
		}
		else if (key.equals(Command.GET_MEDIA_INFO)) {
			settings.put("ENABLED", "true");
			settings.put("FORMAT", MediaInfo.FORMAT_DBUS);
		}
		return settings;
	}
	
	Map<String, String> vlcSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "25.0");
		}
		return settings;
	}

	Map<String, String> dragonPlayerSettings(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		if (key.equals(Command.VOL_CURRENT)) {
			settings.put("MIN", "0.0");
			settings.put("MAX", "100.0");
		}
		return settings;
	}
	
	String rhythmBoxCommand(String key) {
		final String dest = "org.gnome.Rhythmbox";
		List<String> args = new ArrayList<String>();
		String out = null;
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
		else if (key.equals(Command.GET_MEDIA_INFO)) {
			String uriCommand = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
				"org.gnome.Rhythmbox.Player.getPlayingUri");
			out = new RhythmDBHelper().compileQuery(uriCommand);
		}
		else if (key.equals(Command.IS_PLAYING)) {
			out = new DBusHelper().compileMethodCall(dest, "/org/gnome/Rhythmbox/Player", 
				"org.gnome.Rhythmbox.Player.getPlaying");
		}
		
		return out;
	}

	String amarokCommand(String key) {
		final String dest = "org.kde.amarok";
		List<String> args = new ArrayList<String>();
		String out = null;
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
		else if (key.equals(Command.GET_MEDIA_INFO)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.GetMetadata");
		}
		return out;
	}

	String vlcCommand(String key) {
		final String dest = "org.mpris.vlc";
		List<String> args = new ArrayList<String>();
		String out = null;
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
		return out;
	}
	
	String totemCommand(String key) {
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
	
	String dragonPlayerCommand(String key) {
		final String dest = "org.mpris.dragonplayer-$(pidof dragon)";
		List<String> args = new ArrayList<String>();
		String out = null;
		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
			out = new DBusHelper().compileMethodCall(dest, "/Player", 
				"org.freedesktop.MediaPlayer.PlayPause");
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
		return out;
	}
}
