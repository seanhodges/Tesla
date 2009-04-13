/* class MSG_CHANNEL_DATA
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
 * Data is transmitted in a channel in these messages. A channel is
 * bidirectional, and both sides can transmit these messages.
 */
class MSG_CHANNEL_DATA
    extends Packet
    implements IInteractivePacket
{
    /** Use this constructor when receiving a packet from the network.
     */
    MSG_CHANNEL_DATA(byte[] data_) {
	super(data_);
    }

    /** Use this constructor when creating a packet to be sent on
     * the network.
     * Construct a SSH_MSG_CHANNEL_DATA packet containing the 
     * specified data.
     * @param channel_ the remote channel number
     * @param data_ an array of bytes containing the data.
     * @param nbytes_ the number of relevant bytes in the array.
     */
    MSG_CHANNEL_DATA(int channel_, byte[] data_, int nbytes_) {
	super();

	int block_length =
	    1 +			// packet-type
	    4 +			// channel number
	    4 +			// string-length
	    nbytes_;		// string contents

	super._data = new byte[block_length];

	int offset = 0;
	super._data[offset++] = SSH_MSG_CHANNEL_DATA;

	SSHOutputStream.insertInteger(channel_, offset, super._data);
	offset += 4;

	SSHOutputStream.insertInteger(nbytes_, offset, super._data);
	offset += 4;

	for (int i=0; i<nbytes_; i++)
	    super._data[offset++] = data_[i];
    }

    int getRemoteChannelNumber() {
	int offset = 1;	    // skip over packet-type

	return SSHInputStream.getInteger(offset, super._data);
    }

    byte[] getChannelData() {
	// skip packet-type byte and remote channel number
	int offset = 5;

	int length = SSHInputStream.getInteger(offset, super._data);
	offset += 4;

	byte[] channel_data = new byte[length];
	System.arraycopy(super._data, offset, 
	    channel_data, 0,
	    length);

	return channel_data;
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
