/* class MSG_PORT_OPEN
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
 * Sent by either party in interactive mode, this message indicates
 * that a connection has been opened to a forwarded TCP/IP port.
 */
class MSG_PORT_OPEN
    extends Packet
    implements IInteractivePacket
{
    /** Use this constructor when receiving a packet from the network.
     */
    MSG_PORT_OPEN(byte[] data_) {
	super(data_);
    }

    /**
     * @param local_channel_ the channel number that the sending 
     * party has allocated for the connection.
     */
    MSG_PORT_OPEN(int local_channel_, String hostname_,
	int port_) 
    {
	super();

	byte[] host_name = SSHOutputStream.string2Bytes(hostname_);

	int block_length = 
	    1 +				    // packet-type byte
	    4 +				    // local_channel
	    host_name.length +		    // hostname
	    4;				    // port

	super._data = new byte[block_length];

	int offset = 0;
	super._data[offset++] = SSH_MSG_PORT_OPEN;

	SSHOutputStream.insertInteger(local_channel_, offset, super._data);
	offset += 4;

	for (int i=0; i<host_name.length; i++)
	    super._data[offset++] = host_name[i];

	SSHOutputStream.insertInteger(port_, offset, super._data);
    }

    int getChannelNumber() {
	int offset = 1;
	return SSHInputStream.getInteger(offset, super._data);
    }

    String getHostname() {
	int offset = 1;			// skip over packet-type
	offset += 4;			// skip over local_channel
	return SSHInputStream.getString(offset, super._data);
    }

    int getPort() {
	int offset = 1;			// skip over packet-type
	offset += 4;			// skip over local_channel

	// Get length of hostname string
	int string_length = SSHInputStream.getInteger(offset, super._data);

	offset += (4 + string_length);	// skip over hostname
	return SSHInputStream.getInteger(offset, super._data);
    }

    /** Implements the IInteractivePacket interface.
     */
    public void processPacket(IProtocolHandler handler_) {

	boolean allowed = handler_.isPortOpenAllowed(getHostname(), getPort());

	if (allowed) {
	    OpenChannel channel = new OpenChannel(handler_, getHostname(), 
		getPort(), getChannelNumber());
	    Thread channelThread = new Thread(channel);

	    /* We don't want the application to exit until this thread has
	     * exited.
	     */
	    channelThread.setDaemon(false);
	    channelThread.start();
	}
	else {
	    MSG_CHANNEL_OPEN_FAILURE reply = 
		new MSG_CHANNEL_OPEN_FAILURE(getChannelNumber());
	    handler_.enqueueToRemote(reply);
	}
    }
}
