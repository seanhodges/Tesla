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

package tesla.app.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import tesla.app.command.provider.AppConfigProvider;
import tesla.app.command.provider.FallbackConfigProvider;
import tesla.app.command.provider.GlobalConfigProvider;
import tesla.app.command.provider.IConfigProvider;
import tesla.app.command.provider.InitScriptProvider;

public class CommandFactory {
	
	private static final long COMMAND_DELAY = 300;
	private static final long INIT_SCRIPT_DELAY = 1000;

	ArrayList<IConfigProvider> providerScanner = new ArrayList<IConfigProvider>();
	
	public CommandFactory(String initialApp) {
		IConfigProvider globalProvider = new GlobalConfigProvider();
		IConfigProvider appProvider = new AppConfigProvider(initialApp);
		IConfigProvider fallbackProvider = new FallbackConfigProvider();
		
		// Scan the config providers in this order
		providerScanner.add(globalProvider);
		providerScanner.add(appProvider);
		providerScanner.add(fallbackProvider);
	}

	public Command getInitScript() {
		Command out = new Command();
		out.setKey(Command.INIT);
		out.setDelay(INIT_SCRIPT_DELAY);
		out.setCommandString(InitScriptProvider.getInitScript());
		return out;
	}

	public Command getCommand(String key) {
		Command out = new Command();
		out.setKey(key);
		out.setDelay(COMMAND_DELAY);
		Map<String, String> settings = null;
		
		String command = null;
		Iterator<IConfigProvider> providerOrderIt = providerScanner.iterator();
		while (command == null && providerOrderIt.hasNext()) {
			IConfigProvider currentProvider = providerOrderIt.next(); 
			command = currentProvider.getCommand(key);
			settings = currentProvider.getSettings(key);
			// Set the target app name
			if (currentProvider instanceof AppConfigProvider) {
				out.setTargetApp(((AppConfigProvider)currentProvider).getAppName());
			}
		}
		out.setCommandString(command);
		out.setSettings(settings);
		return out;
	}
}
