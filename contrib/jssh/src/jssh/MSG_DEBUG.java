/* class MSG_DEBUG
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
 * This message can be sent by either party at any time; it is used
 * to send debugging messages that may be informative to the user
 * in solving various problems.
 */
class MSG_DEBUG
    extends Packet
    implements IInteractivePacket
{

    MSG_DEBUG(byte[] data_) {
	super(data_);
    }

    /** Returns the data that must be sent to stdout.
     */
    String getMessage() {
	int offset = 1;	// skip packet-type byte

	return SSHInputStream.getString(offset, super._data);
    }

    /** This method implements the IInteractivePacket interface.
     */
    public void processPacket(IProtocolHandler handler_) {
	/* The STDOUT_InputStream class will decide where to send the 
	 * output.
	 */
	handler_.enqueueToStdout(new SMSG_STDERR_DATA(getMessage() + "\r\n"));
    }

    //====================================================================
    // INSTANCE VARIABLES

}
