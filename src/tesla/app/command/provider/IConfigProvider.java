package tesla.app.command.provider;

import java.util.Map;

public interface IConfigProvider {

	public String getCommand(String key) throws Exception;
	public Map<String, String> getSettings(String key);
}
