package uk.sean.command;

import java.util.ArrayList;

public class Command {

	// Command key tokens
	public static final String INIT = "init";
	public static final String PLAY = "play";
	public static final String PAUSE = "pause";
	public static final String PREV = "prev";
	public static final String NEXT = "next";
	public static final String VOL_CHANGE = "vol_change";
	public static final String VOL_MUTE = "vol_mute";

	private String key;
	private String commandString;
	private ArrayList<Object> args;

	public Command() {
		args = new ArrayList<Object>();
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setCommandString(String commandString) {
		this.commandString = commandString;
	}

	public void addArg(Object arg) {
		args.add(arg);
	}

	public String getKey() {
		return key;
	}

	public String getCommandString() {
		String out = "";
		try {
			out = parseArguments(commandString);
		}
		catch (IllegalArgumentException e) {
			// function will return empty string
		}
		return out;
	}

	private String parseArguments(String out)
				throws IllegalArgumentException {
		// Add the arguments to the command
		for (Object arg : args) {
			boolean success = false;
			if (arg instanceof String) {
				if (out.contains("%s")) {
					out = out.replaceFirst("%s", (String)arg);
					success = true;
				}
			}
			else if (arg instanceof Float) {
				if (out.contains("%f")) {
					out = out.replaceFirst("%f", ((Float)arg).toString());
					success = true;
				}
			}
			else if (arg instanceof Integer) {
				if (out.contains("%i")) {
					out = out.replaceFirst("%i", ((Integer)arg).toString());
					success = true;
				}
			}
			else if (arg instanceof Boolean) {
				if (out.contains("%b")) {
					out = out.replaceFirst("%b", ((Boolean)arg).toString());
					success = true;
				}
			}
			if (!success) {
				throw new IllegalArgumentException("Argument " + arg.toString() + " is not valid for command");
			}
		}
		return out;
	}

}
