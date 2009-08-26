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

import java.util.List;
import java.util.Map;

import tesla.app.R;
import tesla.app.command.provider.AppConfigProvider;
import tesla.app.service.connect.ConnectionOptions;
import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class AppSelector extends ListActivity {

	private List<Map<String, String>> providerList;
	private ConnectionOptions config;
	
	protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_selector);
        
        providerList = AppConfigProvider.getAppDirectory();

        ListAdapter providerSelector = new SimpleAdapter(
        		this, providerList, R.layout.app_selector_entry, 
        		new String[] { "icon", "name" }, 
        		new int[] { R.id.selector_app_icon, R.id.selector_app_name });
        setListAdapter(providerSelector);
        
        config = new ConnectionOptions(this);
        
        if (!config.appSelection.equals("")) {
        	// Load the last saved settings
        	setSelection(AppConfigProvider.findAppIndexMatchingName(config.appSelection));
        }
        else {
    		// Default to Rhythmbox
        	setSelection(AppConfigProvider.findAppIndexMatchingName(AppConfigProvider.APP_RHYTHMBOX));
        }
    }
	
	@SuppressWarnings("unchecked")
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Map<String, String> item = (Map<String, String>) getListAdapter().getItem(position);
		config.appSelection = item.get("ref");
		config.saveSettings();
		setResult(Activity.RESULT_OK);
		finish();
	}
}
