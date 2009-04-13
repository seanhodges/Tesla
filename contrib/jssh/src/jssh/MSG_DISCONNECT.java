/* class MSG_DISCONNECT
 *
 * Copyright (C) 2002  R M Pitman <http://www.pitman.co.za>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package jssh;

/**
 * This message can be sent by either party at any time; it causes the
 * immediate disconnection of the connection. It contains a message intended to
 * be displayed to a human, and describes the reason for the disconnection.
 */
class MSG_DISCONNECT
    extends Packet
    implements IInteractivePacket
{

    MSG_DISCONNECT(byte[] data_) {
	super(data_);
    }

    /** Returns the data that must be sent to stdout.
     */
    String getCause() {
	int offset = 1;	// skip packet-type byte

	return SSHInputStream.getString(offset, super._data);
    }

    /** This method implements the IInteractivePacket interface.
     */
    public void processPacket(IProtocolHandler handler_) {
	handler_.enqueueToStdout(this);
    }

    //====================================================================
    // INSTANCE VARIABLES

}
