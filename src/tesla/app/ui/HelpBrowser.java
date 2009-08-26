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

package tesla.app.ui;

import tesla.app.R;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelpBrowser extends Activity {

	private static final String VLC_HELP_LINK = "file:///android_asset/vlc_config.html";
	private static final String VLC_HELP_DATA = "<html><body><h1>VLC set-up instructions</h1><p>VLC does not allow you to interface with it remotely by default. To use it with Tesla, you must first turn on the <i>D-Bus remote interface</i> in the preferences:</p><ol><li>Start the VLC player.</li><li>In the top menu, select <b>Tools</b> and <b>Preferences...</b></li><li>At the bottom of the preferences dialog, select the <b>All</b> radio button under <b>Show settings</b>.</li><li>The left panel should become a tree list, expand the <b>Interfaces</b> group, and click on <b>Control Interfaces</b>.</li><li>Tick the checkbox labeled <b>D-Bus control interface</b>.</li><li>Click <b>Save</b> to close the preferences dialog.</li></ol><p>You should now be able to connect to VLC from Tesla, and your setting should be remembered from now on.</p></body></html>";
	
	private WebView browser;

	protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_browser);
        
        browser = (WebView) findViewById(R.id.webview);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.loadData(VLC_HELP_DATA, "text/html", "utf-8");
    }
}
