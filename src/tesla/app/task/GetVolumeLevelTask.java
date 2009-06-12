package tesla.app.task;

import java.util.Map;

import tesla.app.command.Command;
import tesla.app.command.helper.DBusHelper;
import tesla.app.service.business.ICommandController;
import tesla.app.service.business.IErrorHandler;
import android.os.AsyncTask;
import android.os.RemoteException;

public class GetVolumeLevelTask extends AsyncTask<ICommandController, Boolean, Float> {

	private OnGetVolumeLevelListener listener = null;
	
	ICommandController commandService;
	private Command command;
	
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
	
	public interface OnGetVolumeLevelListener {
		void onGetVolumeExtents(float min, float max);
		void onGetVolumeFailed(String errorTitle, String errorMessage);
		void onGetVolumeComplete(Float result);
	}

	public GetVolumeLevelTask(ICommandController commandService) {
		this.commandService = commandService;
	}

	protected void onPreExecute() {
		float min = 0.0f;
		float max = 1.0f;
		try {
			commandService.registerErrorHandler(errorHandler);
			
			command = commandService.queryForCommand(Command.VOL_CURRENT);
			
			Map<String, String> settings = command.getSettings();
	        min = Float.parseFloat(settings.get("MIN"));
	        max = Float.parseFloat(settings.get("MAX"));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (errorTitle != null && errorMessage != null) {
			if (listener != null) listener.onGetVolumeFailed(errorTitle, errorMessage);
		}
		else {
			if (listener != null) listener.onGetVolumeExtents(min, max);
		}
	}
	
	protected Float doInBackground(ICommandController... args) 
	{
		float volumeLevel = 0.0f;
		ICommandController commandService = args[0];
		
		try {
			command = commandService.queryForCommand(Command.VOL_CURRENT);
			command = commandService.sendQuery(command);
			
			commandService.unregisterErrorHandler(errorHandler);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		// Parse the result as a level percentage
		if (command != null && command.getOutput() != null && command.getOutput() != "") {
			try {
				volumeLevel = Float.parseFloat(new DBusHelper().evaluateOutput(command.getOutput()));
			}
			catch (NumberFormatException e) {
				// If the volume was not parsed correctly, just default to mute
				volumeLevel = 0.0f;
			}
		}
		
		return volumeLevel;
	}
	
	protected void onPostExecute(Float result) {
		if (errorTitle != null && errorMessage != null) {
			if (listener != null) listener.onGetVolumeFailed(errorTitle, errorMessage);
		}
		else {
			if (listener != null) listener.onGetVolumeComplete(result);
		}
	}

	public void registerConnectionListener(OnGetVolumeLevelListener listener) {
		this.listener = listener;
	}
}
