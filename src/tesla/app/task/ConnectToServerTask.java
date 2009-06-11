package tesla.app.task;

import tesla.app.connect.ConnectionListener;
import tesla.app.service.business.ICommandController;
import tesla.app.service.business.IErrorHandler;
import android.os.AsyncTask;
import android.os.RemoteException;

public class ConnectToServerTask extends AsyncTask<ICommandController, Boolean, Boolean> {

	private ConnectionListener listener = null;
	
	// Error messages need to be passed back to main UI thread
	private String errorTitle = null;
	private String errorMessage = null;
	
	private IErrorHandler errorHandler = new IErrorHandler.Stub() {
		public void onServiceError(String title, String message, boolean fatal) throws RemoteException {
			// Pass the error data back to the main UI thread
			errorTitle = title;
			errorMessage = message;
		}
	};
	
	protected Boolean doInBackground(ICommandController... args) {
		ICommandController commandService = args[0];
		boolean success = false;
		try {
			commandService.registerErrorHandler(errorHandler);
			success = commandService.connect();
			commandService.unregisterErrorHandler(errorHandler);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return success;
	}
	
	protected void onPostExecute(Boolean result) {
		// Pass error message back to UI thread if there is one
		if (errorTitle != null && errorMessage != null) {
			if (listener != null) listener.connectionFailed(errorTitle, errorMessage);
		}
		else {
			if (listener != null) listener.connectionComplete();
		}
	}

	public void registerConnectionListener(ConnectionListener listener) {
		this.listener = listener;
	}
}
