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

package tesla.app.service.connect;

import tesla.app.command.Command;

public class FakeConnection implements IConnection {

	public void connect(ConnectionOptions config) throws ConnectionException {
		// Do nothing
	}

	public void disconnect() {
		// Do nothing
	}

	public boolean isConnected() {
		return true;
	}

	public String sendCommand(Command command) throws ConnectionException {
		if (command.getKey().equals(Command.INIT)) {
			// Simulate the delay used in SSHConnection
			try {
				Thread.sleep(command.getDelay());
			} catch (InterruptedException e) {
				// Do nothing
			}
			return "success\n";
		}
		else if (command.getKey().equals(Command.VOL_CURRENT)) {
			return "method return sender=:1.66 -> dest=:1.1267 reply_serial=2\n   int32 50";
		}
		else {
			return "";
		}
	}

}
