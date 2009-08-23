package tesla.app.command.helper;

import java.util.Map;

public interface ICommandHelper {
	public String evaluateOutputAsString(String rawOut);
	public boolean evaluateOutputAsBoolean(String rawOut);
	public Map<String, String> evaluateOutputAsMap(String rawOut);
}
