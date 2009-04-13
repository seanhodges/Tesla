/* class CMSG_STDIN_DATA
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
 * This message delivers data from the client to be supplied as input to the
 * shell or program running on the server side. It can only be used in the
 * interactive session mode. No acknowledgement is sent for this message.
 */
class CMSG_STDIN_DATA
    extends Packet
{
    /** Construct a SSH_CMSG_STDIN_DATA packet containing the 
     * specified data.
     */
    CMSG_STDIN_DATA(byte[] data_) {
	super();

	int block_length =
	    1 +			// packet-type
	    4 +			// string-length
	    data_.length;	// string contents

	super._data = new byte[block_length];

	int offset = 0;
	super._data[offset++] = SSH_CMSG_STDIN_DATA;

	SSHOutputStream.insertInteger(data_.length, offset, super._data);
	offset += 4;

	for (int i=0; i<data_.length; i++)
	    super._data[offset++] = data_[i];
    }
}
