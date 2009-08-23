package tesla.app.command.helper;

import java.util.Map;

public class SimpleStringHelper implements ICommandHelper {
	
	public boolean evaluateOutputAsBoolean(String rawOut) {
		return Boolean.parseBoolean(rawOut);
	}

	public Map<String, String> evaluateOutputAsMap(String rawOut) {
		return null;
	}

	public String evaluateOutputAsString(String rawOut) {
		return rawOut;
	}

}
