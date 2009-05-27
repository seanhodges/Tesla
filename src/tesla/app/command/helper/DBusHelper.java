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

public class DBusHelper {

	public String compileMethodCall(String dest, String path, String command,
			List<String> args) {
		String out = "dbus-send --session --print-reply --dest=" + dest 
			+ " --type=\"method_call\" " + path + " " + command;
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

	public String evaluateArg(String rawArg) {
		String dataType = "string:";
		if (rawArg.equals("%b") || rawArg.equals("false") || rawArg.equals("true")) {
			dataType = "boolean:";
		}
		else if (rawArg.equals("%i")) {
			dataType = "int32:";
		}
		else if (rawArg.equals("%f")) {
			dataType = "double:";
		}
		else if (rawArg.contains(".")) {
			// Attempt to parse as a float
			try {
				@SuppressWarnings("unused")
				float test = Float.valueOf(rawArg);
				dataType = "double:";
			}
			catch (NumberFormatException e) {
				// Continue
			}
		}
		else {
			// Attempt to parse as an integer
			try {
				@SuppressWarnings("unused")
				int test = Integer.valueOf(rawArg);
				dataType = "int32:";
			}
			catch (NumberFormatException e) {
				// Continue
			}
		}
		return dataType + rawArg;
	}
	
	public String evaluateOutput(String rawOut) {
		String out = rawOut;
		if (rawOut.contains("\n   ")) {
			out = rawOut.split("\n   ")[1];
			if (out.startsWith("int32")) {
				out = out.substring(6);
			}
			else if (out.startsWith("double")) {
				out = out.substring(7);
			}
			else if (out.startsWith("string")) {
				out = out.substring(7);
			}
			else if (out.startsWith("boolean")) {
				out = out.substring(8);
			}
			else {
				// Data type not recognised, return empty string
				out = "";
			}
			out = out.trim();
		}
		return out;
	}
}
