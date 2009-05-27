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

package tesla.app.connect;

public class ConnectionException extends Exception {
	private static final long serialVersionUID = 1L;
	
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
