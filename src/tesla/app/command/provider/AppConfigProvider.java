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
import tesla.app.command.provider.app.AmarokConfig;
import tesla.app.command.provider.app.DragonPlayerConfig;
import tesla.app.command.provider.app.RhythmboxConfig;
import tesla.app.command.provider.app.TotemConfig;
import tesla.app.command.provider.app.VlcConfig;

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
		String out = null;
		if (appName.equals(APP_RHYTHMBOX)) {
			out = new RhythmboxConfig().getCommand(key);
		}
		else if (appName.equals(APP_AMAROK)) {
			out = new AmarokConfig().getCommand(key);
		}
		else if (appName.equals(APP_VLC)) {
			out = new VlcConfig().getCommand(key);
		}
		else if (appName.equals(APP_TOTEM)) {
			out = new TotemConfig().getCommand(key);
		}
		else if (appName.equals(APP_DRAGONPLAYER)) {
			out = new DragonPlayerConfig().getCommand(key);
		}
		return out;
	}
	
	public Map<String, String> getSettings(String key) {
		Map<String, String> settings = null;	
		if (appName.equals(APP_RHYTHMBOX)) {
			settings = new RhythmboxConfig().getSettings(key);
		}
		else if (appName.equals(APP_AMAROK)) {
			settings = new AmarokConfig().getSettings(key);
		}
		else if (appName.equals(APP_VLC)) {
			settings = new VlcConfig().getSettings(key);
		}
		else if (appName.equals(APP_DRAGONPLAYER)) {
			settings = new DragonPlayerConfig().getSettings(key);
		}
		else if (appName.equals(APP_TOTEM)) {
			settings = new TotemConfig().getSettings(key);
		}
		return settings;
	}

	public String getAppName() {
		return appName;
	}
}
