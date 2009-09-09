package tesla.app.service;

import tesla.app.service.connect.IConnection;
import android.app.Service;

public abstract class ConnectionHolder extends Service {

	private volatile IConnection connection;
	private volatile boolean connectionBusy = false;

	public void setConnection(IConnection connection) {
		this.connection = connection;
	}

	public synchronized IConnection getConnection() {
		while (connectionBusy) {
			// Block until connection is available
		}
		connectionBusy = true;
		return connection;
	}
	
	public void freeConnection() {
		connectionBusy = false;
	}
}
