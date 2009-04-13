package uk.sean.connect;

public class ConnectionException extends Exception {
	
	public static final int FAILED_AT_CONNECT = 10;
	public static final int FAILED_AT_AUTH = 20;
	public static final int FAILED_AT_INIT = 30;
	public static final int FAILED_AT_COMMAND = 40;
	
	private String message = "";
	
	public ConnectionException(int status, String identifier, String detailMessage) {
		switch (status) {
		case FAILED_AT_CONNECT:
			detailMessage = "[" + identifier + "] Error whilst connecting to device: " + detailMessage;
			break;
		case FAILED_AT_AUTH:
			detailMessage = "[" + identifier + "] Error whilst authenticating with connected device: " + detailMessage;
			break;
		case FAILED_AT_INIT:
			detailMessage = "[" + identifier + "] Error during initialisation of session: " + detailMessage;
			break;
		case FAILED_AT_COMMAND:
			detailMessage = "[" + identifier + "] Could not send command to device: " + detailMessage;
			break;
		}
		message = detailMessage;
	}
	
	public String getMessage() {
		return message;
	}
}
