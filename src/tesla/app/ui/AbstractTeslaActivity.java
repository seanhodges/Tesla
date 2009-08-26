/* Copyright 2009 Sean Hodges <seanhodges@bluebottle.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tesla.app.ui;

import tesla.app.service.CommandService;
import tesla.app.service.business.ICommandController;
import tesla.app.service.business.IErrorHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.view.KeyEvent;

public abstract class AbstractTeslaActivity extends Activity {
	
	protected ICommandController commandService;
	private PowerManager.WakeLock wakeLock;
	
	protected ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			commandService = ICommandController.Stub.asInterface(service);
			onTeslaServiceConnected();
		}

		public void onServiceDisconnected(ComponentName name) {
			commandService = null;
			onTeslaServiceDisconnected();
		}
	};
	
	protected IErrorHandler errorHandler = new IErrorHandler.Stub() {
		public void onServiceError(String title, String message, boolean fatal) throws RemoteException {
			showErrorMessage(title, message);
		}
	};
	
	public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        // Used to keep the Wifi available as long as the activity is running
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Tesla SSH session");
    }
	
	protected void onPause() {
		super.onPause();
		if (connection != null) unbindService(connection);
		wakeLock.release();
	}

	protected void onResume() {
		super.onResume();
		wakeLock.acquire();
		bindService(new Intent(AbstractTeslaActivity.this, CommandService.class), connection, Context.BIND_AUTO_CREATE);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:
			// If the HOME button is pressed, the application is shutting down.
			// Therefore, stop the service...
			stopService(new Intent(AbstractTeslaActivity.this, CommandService.class));
		}
		return super.onKeyDown(keyCode, event); 
	}
	
	protected abstract void onTeslaServiceConnected();
	protected abstract void onTeslaServiceDisconnected();

	protected void showErrorMessage(String title, String message) {
		if (!isFinishing()) {
			new AlertDialog.Builder(AbstractTeslaActivity.this)
				.setTitle(title)
				.setMessage(message)
				.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.show();
		}
	}
}
