/* class SMSG_STDERR_DATA
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
 * Delivers data from the server that was read from the standard 
 * error stream of the shell or program running on the server side.
 * This message can only be used in the interactive session mode.
 * No acknowledgement is sent for this message.
 */
class SMSG_STDERR_DATA
    extends Packet
    implements IInteractivePacket
{
    /** Use this constructor when receiving a packet from the network.
     */
    SMSG_STDERR_DATA(byte[] data_) {
	super(data_);
    }

    /** Use this constructor when constructing a packet that must be 
     * sent to STDERR as though it came from the server.
     */
    SMSG_STDERR_DATA(String str_) {
	super();
	
	byte[] bytes = str_.getBytes();

	int block_length =
	    1 +			// packet-type
	    4 +			// string-length
	    bytes.length;	// string contents

	super._data = new byte[block_length];

	int offset = 0;
	super._data[offset++] = SSH_SMSG_STDERR_DATA;

	SSHOutputStream.insertInteger(bytes.length, offset, super._data);
	offset += 4;

	for (int i=0; i<bytes.length; i++)
	    super._data[offset++] = bytes[i];
    }

    /** Returns the data that must be sent to stdout/stderr.
     */
    String getStderrData() {
	int offset = 1;	// skip packet-type byte

	return SSHInputStream.getString(offset, super._data);
    }

    /** This method implements the IInteractivePacket interface.
     */
    public void processPacket(IProtocolHandler handler_) {
	/* The STDOUT_InputStream class will decide where to send
	 * the output.
	 */
	handler_.enqueueToStdout(this);
    }

}
