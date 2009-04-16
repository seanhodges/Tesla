package uk.sean.command;

public class Command {

	// Command key tokens
	public static final String INIT = "init";
	public static final String PLAY = "play";
	public static final String PAUSE = "pause";
	public static final String PREV = "prev";
	public static final String NEXT = "next";

	private String key;
	private String commandString;

	public void setKey(String key) {
		this.key = key;
	}

	public void setCommandString(String commandString) {
		this.commandString = commandString;
	}

	public String getKey() {
		return key;
	}

	public String getCommandString() {
		return commandString;
	}

}
