package tesla.app.connect;

public interface ConnectionListener {
	
	void connectionCancelled();
	void connectionComplete();
	void connectionFailed(String title, String message);
}
