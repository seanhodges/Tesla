package uk.sean.command.provider;


public class InitScriptProvider {
	
	public static String getInitScript() {
		return script;
	}
	
	private static String script = "" +
			"compatiblePrograms=( nautilus kdeinit pulseaudio trackerd ); " +
			
			"for index in ${compatiblePrograms[@]}; do " +
			"	PID=$(pidof -s ${index}); " +
			"	if [[ \"${PID}\" != \"\" ]]; then break; fi; " +
			"done; " +
			
			"if [[ \"${PID}\" == \"\" ]]; then echo Could not detect active login session; fi; " +
			
			"QUERY_ENVIRON=\"$(tr '\\0' '\\n' < /proc/${PID}/environ | grep 'DBUS_SESSION_BUS_ADDRESS' | cut -d '=' -f 2-)\"; " +
			
			"if [[ \"${QUERY_ENVIRON}\" != \"\" ]]; then " +
			"	export DBUS_SESSION_BUS_ADDRESS=\"${QUERY_ENVIRON}\"; " +
			"	echo success; " +
			"else " +
			"	echo Could not find dbus session ID in user environment; " +
			"fi; " +
			
			"";
}
