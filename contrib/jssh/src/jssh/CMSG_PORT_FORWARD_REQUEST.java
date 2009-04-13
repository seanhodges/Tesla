/* class CMSG_PORT_FORWARD_REQUEST
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
 * Sent by the client in the preparatory phase, this message
 * requests that server_port_ on the server machine be forwarded
 * over the secure channel to the client machine, and from there
 * to the specified host and port.
 */
class CMSG_PORT_FORWARD_REQUEST
    extends Packet
{

    /**
     * Construct a CMSG_PORT_FORWARD_REQUEST packet.
     * @param server_port_ the port that the server is requested
     * to listen on for TCP connections.
     * @param host_to_connect_ the hostname that the server must
     * insert into the SSH_MSG_PORT_OPEN message that it sends to the
     * client when a connection is made to it.
     * @param port_to_connect_ the port number that the server must
     * insert into the SSH_MSG_PORT_OPEN message.
     */
    CMSG_PORT_FORWARD_REQUEST(int server_port_,
	    String host_to_connect_, int port_to_connect_) 
    {
	super();

	byte[] host = SSHOutputStream.string2Bytes(host_to_connect_);

	int packet_length = 
	    1 +				    // packet-type byte
	    4 +				    // server port
	    host.length +		    // host_to_connect
	    4;				    // port_to_connect

	super._data = new byte[packet_length];

	int offset = 0;
	super._data[offset++] = SSH_CMSG_PORT_FORWARD_REQUEST;

	SSHOutputStream.insertInteger(server_port_, offset, super._data);
	offset += 4;

	for (int i=0; i<host.length; i++)
	    super._data[offset++] = host[i];

	SSHOutputStream.insertInteger(port_to_connect_, offset, super._data);
    }
}
