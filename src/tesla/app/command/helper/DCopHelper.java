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

package tesla.app.command.helper;

import java.util.List;

public class DCopHelper {

	public String compileMethodCall(String dest, String path, String command,
			List<String> args) {
		String out = "dcop --all-users " + dest + " " + path + " " + command;
		if (args != null) {
			for (String arg : args) {
				out += " " + arg;
			}
		}
		return out;
	}

	public String compileMethodCall(String dest, String path, String command) {
		return compileMethodCall(dest, path, command, null);
	}
	
	public String evaluateOutputAsString(String rawOut) {
		return evaluateOutputAsString(rawOut, true);
	}
	
	private String evaluateOutputAsString(String rawOut, boolean primitive) {
		return rawOut;
	}
	
	public boolean evaluateOutputAsBoolean(String rawOut) {
		String data = evaluateOutputAsString(rawOut, true);
		return Boolean.parseBoolean(data);
	}
}
