package tesla.test.ssh;

import java.util.regex.Pattern;

import java.util.regex.Matcher;

import android.test.AndroidTestCase;
import tesla.app.command.Command;
import tesla.app.command.provider.app.AmarokConfig;
import tesla.app.service.connect.ConnectionException;
import tesla.app.service.connect.ConnectionOptions;
import tesla.app.service.connect.SSHConnection;

public class SynchronousStabilityTest extends AndroidTestCase {
	
	private static final SSHConnection connection = new SSHConnection();
	
	public void setUp() throws Exception {
		connection.connect(new ConnectionOptions(this.getContext()) {
			{
				this.hostname = "10.0.2.2";
				this.username = "username";
				this.password = "password";
				this.port = 22;
			}
			
			public void reloadSettings() {}
			public void saveSettings() {}
		});
	}
	
	public void testDbusCommandSucceedsWhenCalledInSuccession() throws Exception {
		String commandString = new AmarokConfig().getCommand(Command.GET_MEDIA_INFO);
		Command command = new Command();
		command.setCommandString(commandString);
		
		Pattern expectedOutput = Pattern.compile("\\[dcop\\]\\n" +
				"tracknumber:[0-9]*\\n" +
				"title:[&\\(\\)\\w ]*\\n" +
				"artist:[\\w ]*\\n" +
				"album:[\\w ]*\\n" +
				"[\\S\\s]*");
		
		// Send command X times, and check that the output is correct each time
		for (int i = 0; i < 1000; i++) {
			String output = null;
			try {
				output = connection.sendCommand(command);
			}
			catch (ConnectionException e) {
				fail("Failed at attempt " + (i + 1) + ", error was " + e);
			}
			Matcher matcher = expectedOutput.matcher(output);
			assertTrue("Failed at attempt " + (i + 1) + ", output was " + output, matcher.matches());
		}
	}
	
	public void tearDown() throws Exception {
		connection.disconnect();
	}
}
