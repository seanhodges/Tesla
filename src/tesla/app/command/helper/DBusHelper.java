package tesla.app.command.helper;

import java.util.List;

public class DBusHelper {

	public String compileMethodCall(String dest, String path, String command,
			List<String> args) {
		String out = "dbus-send --session --dest=" + dest 
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
		if (rawOut.contains(":")) {
			out = rawOut.split(":")[1]; 
		}
		return out;
	}
}
