/* class SMSG_STDOUT_DATA
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
 * Delivers data from the server that was read from the standard output of the
 * shell or program running on the server side. This message can only be used
 * in the interactive mode. No acknowledgement is sent for this message.
 */
class SMSG_STDOUT_DATA
    extends Packet
    implements IInteractivePacket
{

    SMSG_STDOUT_DATA(byte[] data_) {
	super(data_);
    }

    /** Returns the data that must be sent to stdout.
     */
    byte[] getStdoutData() {
	int offset = 1;	// skip packet-type byte

	int length = 0;
	length = ((int) super._data[offset++] & 0xff) << 24;
	length |= ((int) super._data[offset++] & 0xff) << 16;
	length |= ((int) super._data[offset++] & 0xff) << 8;
	length |= super._data[offset++] & 0xff;

	byte[] stdout_data = new byte[length];
	System.arraycopy(super._data, offset, 
	    stdout_data, 0,
	    length);

	return stdout_data;
    }

    /** This method implements the IInteractivePacket interface.
     */
    public void processPacket(IProtocolHandler handler_) {
	handler_.enqueueToStdout(this);
    }

    //====================================================================
    // INSTANCE VARIABLES

}
