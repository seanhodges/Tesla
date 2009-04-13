/* class SMSG_EXITSTATUS
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
 * Returns the exit status of the shell or program after it has exited.
 * The client should respond with SSH_CMSG_EXIT_CONFIRMATION.
 */
class SMSG_EXITSTATUS
    extends Packet
    implements IInteractivePacket
{

    SMSG_EXITSTATUS(byte[] data_) {
	super(data_);
    }

    int getExitStatus() {
	int offset = 1;	    // skip over packet-type
	return SSHInputStream.getInteger(offset, super._data);
    }

    /** This method implements the IInteractivePacket interface.
     */
    public void processPacket(IProtocolHandler handler_) {
	CMSG_EXIT_CONFIRMATION confirmation_packet =
	    new CMSG_EXIT_CONFIRMATION();

	handler_.enqueueToRemote(confirmation_packet);
    }
}
