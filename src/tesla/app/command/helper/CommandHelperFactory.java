package tesla.app.command.helper;

import tesla.app.command.Command;

public class CommandHelperFactory {
	
	public static final ICommandHelper getHelperForCommand(Command command) {
		ICommandHelper out;
		String data = command.getOutput();
		if (data.startsWith(DBusHelper.MAGIC_MARKER)) {
			command.setOutput(command.getOutput().substring(DBusHelper.MAGIC_MARKER.length() + 1));
			out = new DBusHelper();
		}
		else if (data.startsWith(DCopHelper.MAGIC_MARKER)) {
			command.setOutput(command.getOutput().substring(DCopHelper.MAGIC_MARKER.length() + 1));
			out = new DCopHelper();
		}
		else if (data.startsWith(RhythmDBHelper.MAGIC_MARKER)) {
			command.setOutput(command.getOutput().substring(RhythmDBHelper.MAGIC_MARKER.length() + 1));
			out = new RhythmDBHelper();
		}
		else if (data.startsWith(ExaileHelper.MAGIC_MARKER)) {
			command.setOutput(command.getOutput().substring(ExaileHelper.MAGIC_MARKER.length() + 1));
			out = new ExaileHelper();
		}
		else if (data.startsWith(AmarokPlaylistHelper.MAGIC_MARKER)) {
			command.setOutput(command.getOutput().substring(AmarokPlaylistHelper.MAGIC_MARKER.length() + 1));
			out = new AmarokPlaylistHelper();
		}
		else if (data.startsWith(RelativePlaylistHelper.MAGIC_MARKER)) {
			command.setOutput(command.getOutput().substring(RelativePlaylistHelper.MAGIC_MARKER.length() + 1));
			out = new RelativePlaylistHelper();
		}
		else {
			out = new SimpleStringHelper();
		}
		return out;
	}
	
}
