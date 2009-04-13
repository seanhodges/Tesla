/* class MSG_CHANNEL_OPEN_FAILURE
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
 * This message indicates that an earlier channel open request
 * by the other side has failed or has been denied.
 */
class MSG_CHANNEL_OPEN_FAILURE
    extends Packet
    implements IInteractivePacket
{
    /** Use this constructor when receiving a packet from the network.
     */
    MSG_CHANNEL_OPEN_FAILURE(byte[] data_) {
	super(data_);
    }

    /**
     * Use this constructor when creating a packet to be sent on
     * the network.
     * @param remote_channel_ the channel number given in the original open
     * request.
     */
    MSG_CHANNEL_OPEN_FAILURE(int remote_channel_) 
    {
	super();

	int block_length = 
	    1 +				    // packet-type byte
	    4;				    // remote channel

	super._data = new byte[block_length];

	int offset = 0;
	super._data[offset++] = SSH_MSG_CHANNEL_OPEN_FAILURE;

	SSHOutputStream.insertInteger(remote_channel_, offset, super._data);
    }

    int getRemoteChannelNumber() {
	int offset = 1;	    // skip over packet-type

	return SSHInputStream.getInteger(offset, super._data);
    }

    /** Implements the IInteractivePacket interface.
     */
    public void processPacket(IProtocolHandler handler_) {
	OpenChannel channel =
	    handler_.findOpenChannel(getRemoteChannelNumber());
	if (channel != null)
	    channel.enqueue(this);
    }
}
