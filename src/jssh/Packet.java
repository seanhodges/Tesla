/* class Packet
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
 * This is the superclass for all the various types of SSH protocol 
 * packets 
 */
abstract class Packet
    implements IPacketConstants
{
    /** Use the default constructor when you don't know the size of
     * the data array in advance.
     */
    protected Packet() {
    }

    /** Use this constructor for packets that have no arguments.
     */
    protected Packet(int type_) {
	int packet_length = 1;	    // packet-type byte

	_data = new byte[1];
	_data[0] = (byte) type_;
    }

    /** Use this constructor when receiving a packet from the network.
     */
    protected Packet(byte[] data_) {
	_data = data_;
    }

    /** Use this constructor when creating a packet to be sent
     * on the network.
     * @param type_ the packet-type.
     * @param data_ the payload byte-array.
     */
    protected Packet(int type_, byte[] data_) {
	
	int packet_length = 1 +	    // packet-type byte
	    data_.length;

	_data = new byte[packet_length];
	_data[0] = (byte) type_;
	System.arraycopy(data_, 0, 
	    _data, 1, 
	    data_.length);
    }

    byte getPacketType() { return _data[0]; }

    /** Returns the byte array including
     * the packet-type byte and the binary data bytes
     */
    byte[] getData() { return _data; }

    //====================================================================
    // INSTANCE VARIABLES

    /** This byte array represents the data block, EXCLUDING the padding 
     * bytes and the CRC (in other words, it contains just the packet-type
     * byte and the binary data bytes).
     */
    protected byte[] _data = null;

}
