package tesla.app.connect;

import android.app.Activity;
import android.content.SharedPreferences;

public class ConnectionOptions {
	
	private static final String PREFS_NAME = "ConnectionSettings";
	
	private Activity owner;
	
	public String hostname = null;
	public int port = 0;
	public String username = null;
	public String password = null;
	
	public ConnectionOptions(Activity owner) {
		this.owner = owner;
		reloadSettings();
	}
	
	public void reloadSettings() {
		// This is a simple load/save mechanism from a simple file
		// TODO: replace/extend with a profile manager
		SharedPreferences settings = owner.getSharedPreferences(PREFS_NAME, 0);
		hostname = settings.getString("hostname", "192.168.0.1");
		port = settings.getInt("port", 22);
		username= settings.getString("username", "User");
		password = settings.getString("password", "");
	}
	
	public void saveSettings() {
		SharedPreferences settings = owner.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("hostname", hostname);
		editor.putInt("port", port);
		editor.putString("username", username);
		editor.putString("password", password);
		editor.commit();
	}
}
