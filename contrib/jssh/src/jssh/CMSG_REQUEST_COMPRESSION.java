/* class CMSG_REQUEST_COMPRESSION
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
 * This message can be sent by the client in the preparatory
 * operations phase. The server responds with SSH_SMSG_FAILURE 
 * if it does not support compression or it does not want to 
 * compress; it responds with SSH_SMSG_SUCCESS if it accepted the
 * compression request. In the latter case the response to this
 * packet will still be uncompressed, but all further packets in
 * both directions will be compressed by gzip.
 */
class CMSG_REQUEST_COMPRESSION
    extends Packet
{
    /**
     * Construct a SSH_CMSG_REQUEST_COMPRESSION packet.
     * @param level_ the gzip compression level (1 - 9).
     */
    CMSG_REQUEST_COMPRESSION(int level_) 
    {
	super();

	int block_length = 
	    1 +				    // packet-type byte
	    4;				    // compression level

	super._data = new byte[block_length];

	int offset = 0;
	super._data[offset++] = SSH_CMSG_REQUEST_COMPRESSION;

	SSHOutputStream.insertInteger(level_, offset, super._data);
    }
}
