package tesla.app.command;

import java.util.ArrayList;
import java.util.Map;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public final class Command implements Parcelable {

	// Command key tokens
	public static final String INIT = "init";
	public static final String PLAY = "play";
	public static final String PAUSE = "pause";
	public static final String PREV = "prev";
	public static final String NEXT = "next";
	public static final String VOL_CHANGE = "vol_change";
	public static final String VOL_MUTE = "vol_mute";
	public static final String VOL_CURRENT = "vol_get_current";

	private String key;
	private String commandString;
	private Map<String, String> settings;
	
	private ArrayList<Object> args;
	long executionDelay = 0;
	private String output;

	public static final Parcelable.Creator<Command> CREATOR = new Parcelable.Creator<Command>() {
		public Command createFromParcel(Parcel in) {
			return new Command(in);
		}
		
		public Command[] newArray(int size) {
			return new Command[size];
		}
	};

	public Command() {
		args = new ArrayList<Object>();
	}
	
	public Command(Parcel in) {
		readFromParcel(in);
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
	
	public void setDelay(long executionDelay) {
		this.executionDelay = executionDelay;
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

	public void setSettings(Map<String, String> settings) {
		this.settings = settings;
	}

	public Map<String, String> getSettings() {
		return settings;
	}

	public long getDelay() {
		return executionDelay;
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

	public int describeContents() {
		return key.hashCode();
	}
	
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(key);
		dest.writeList(args);
		dest.writeString(commandString);
		Bundle settingsBundle = new Bundle();
		for (String key : settings.keySet()) {
			settingsBundle.putString(key, settings.get(key));	
		}
		dest.writeBundle(settingsBundle);
		dest.writeLong(executionDelay);
		dest.writeString(output);
	}

	@SuppressWarnings("unchecked")
	public void readFromParcel(Parcel src) {
		key = src.readString(); 
		args = src.readArrayList(this.getClass().getClassLoader());
		commandString = src.readString();
		Bundle settingsBundle = src.readBundle(this.getClass().getClassLoader());
		settings.clear();
		for (String key : settingsBundle.keySet()) {
			settings.put(key, settingsBundle.getString(key));
		}
		executionDelay = src.readLong();
		output = src.readString();
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getOutput() {
		return output;
	}
}
