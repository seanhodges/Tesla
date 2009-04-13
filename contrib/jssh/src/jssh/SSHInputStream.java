/* class SSHInputStream
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

import java.io.*;
import java.net.*;
import de.mud.ssh.Cipher;

/**
 * This class allows the application to read SSH packets from a network
 * socket. It decrypts and uncompresses the packet if appropriate, and returns an
 * instance of the appropriate subclass of Packet.
 */
public class SSHInputStream 
    extends DataInputStream 
    implements IPacketConstants
{
    /** Constructor
     */
    public SSHInputStream(InputStream stream) 
    {
	super(stream);
    }

    /**
     * Read an SSH packet from the network. The packet is automatically
     * decrypted if the protocol is in encrypted mode. The CRC of the
     * decrypted packet is checked.
     * @return an instance of the appropriate subclass of Packet.
     * @exception SSHProtocolException if the CRC is incorrect, or an
     * unknown packet type is received.
     * @exception EOFException if the server closes the connection.
     * @exception IOException if some network error occurs.
     */
    public Packet readPacket()
	throws IOException, SSHProtocolException
    {
	int packet_length = 0;
	    packet_length = super.readInt();

	int block_length = 8 * ((packet_length / 8) + 1);

	byte[] block = new byte[block_length];

	int offset = 0;
	for (int bytes_left = block_length; bytes_left > 0; ) {
	    int n = super.read(block, offset, bytes_left);
	    if (n == -1)
		throw new EOFException();

	    offset += n;
	    bytes_left -= n;
	}
	int padding_length = 8 - (packet_length % 8);

	// We decrypt here if we are in encrypted mode.
	if (_cipher != null) {
	    block = _cipher.decrypt(block);
	}

	// Check the CRC
	long crcCalculated = SSHMisc.crc32(block, block.length - 4);
	int crcOffset = block.length - 4;
	long crcReceived = getInteger(crcOffset, block) & 0xffffffffL;
	if (crcReceived != crcCalculated)
	    throw new SSHProtocolException("CRC error in received packet");

	byte[] data = new byte[block.length - padding_length - 4];
	System.arraycopy(block, padding_length,
	    data, 0,
	    data.length);

	// We decompress here if compression is enabled.
	if (_inflater != null) {

	    _inflater.setInput(data);
	    int nbytes = _inflater.inflate(uncompressed_bytes, GZInflater.Z_PARTIAL_FLUSH);
	    data = new byte[nbytes];
	    System.arraycopy(uncompressed_bytes, 0, data, 0, data.length);
	}

	// Construct an instance of the appropriate subclass of Packet.
	byte packet_type = data[0];
	switch (packet_type) {
	    case SSH_MSG_DISCONNECT:
		return new MSG_DISCONNECT(data);

	    case SSH_SMSG_PUBLIC_KEY:
		return new SMSG_PUBLIC_KEY(data);

	    case SSH_SMSG_AUTH_RSA_CHALLENGE:
		return new SMSG_AUTH_RSA_CHALLENGE(data);

	    case SSH_SMSG_SUCCESS:
		return new SMSG_SUCCESS(data);

	    case SSH_SMSG_FAILURE:
		return new SMSG_FAILURE(data);

	    case SSH_SMSG_STDOUT_DATA:
		return new SMSG_STDOUT_DATA(data);

	    case SSH_SMSG_STDERR_DATA:
		return new SMSG_STDERR_DATA(data);

	    case SSH_SMSG_EXITSTATUS:
		return new SMSG_EXITSTATUS(data);

	    case SSH_MSG_CHANNEL_OPEN_CONFIRMATION:
		return new MSG_CHANNEL_OPEN_CONFIRMATION(data);

	    case SSH_MSG_CHANNEL_OPEN_FAILURE:
		return new MSG_CHANNEL_OPEN_FAILURE(data);

	    case SSH_MSG_CHANNEL_DATA:
		return new MSG_CHANNEL_DATA(data);

	    case SSH_MSG_CHANNEL_CLOSE:
		return new MSG_CHANNEL_CLOSE(data);

	    case SSH_MSG_CHANNEL_CLOSE_CONFIRMATION:
		return new MSG_CHANNEL_CLOSE_CONFIRMATION(data);

	    case SSH_MSG_PORT_OPEN:
		return new MSG_PORT_OPEN(data);

	    case SSH_MSG_DEBUG:
		return new MSG_DEBUG(data);

	    case SSH_MSG_IGNORE:
		return new MSG_IGNORE();

	    default:
		throw new SSHProtocolException(
		    "received unknown packet type " + packet_type);
	}
    }

    public void setCipher(Cipher cipher_) {
	_cipher = cipher_;
    }

    public void setCompression() {
	_inflater = new GZInflater();
    }

    /**
     * Return the string that is encoded at position "offset" 
     * in the specified byte array.
     * First 4 bytes are the length of the string, msb first (not
     * including the length itself).  The following "length" bytes are
     * the string value.  There are no terminating null characters.
     * @param offset the integer offset of the start of the encoded string.
     * @param byteArray the byte array containing the encoded string.
     * @return the String extracted from the byte array.
     */
    static public String getString(int offset, byte[] byteArray) {

        int length =  (byteArray[offset++] & 0xff) << 24;
        length |= (byteArray[offset++] & 0xff) << 16;
	length |= (byteArray[offset++] & 0xff) << 8;
	length |= (byteArray[offset++] & 0xff);
        StringBuffer buf = new StringBuffer();
        for (int i=0;i<length;i++) {
            buf.append((char)(byteArray[offset++] & 0xff));
        }
        return buf.toString();
    }

    /** Returns the 32-bit integer encoded at position "offset" in the
     * specified byte array.
     */
    static public int getInteger(int offset, byte[] byteArray) {
	int integer = (byteArray[offset++] & 0xff) << 24;
	integer |= (byteArray[offset++] & 0xff) << 16;
	integer |= (byteArray[offset++] & 0xff) << 8;
	integer |= byteArray[offset++] & 0xff;
	return integer;
    }

    /**
     * Return the multiprecision-integer at the position "offset" in the
     * specified byte array.
     * First 2 bytes are the number of bits in the integer, msb first
     * (for example, the value 0x00012345 would have 17 bits).  The
     * value zero has zero bits.  It is permissible that the number of
     * bits be larger than the real number of bits.
     * The number of bits is followed by (bits + 7) / 8 bytes of binary
     * data, msb first, giving the value of the integer.
     */
    static public byte[] getMpInt(int offset, byte[] byteArray) {

	int bitLength = (byteArray[offset++] & 0xff) << 8;
	bitLength |= (byteArray[offset++] & 0xff);

        int byteLength =  (bitLength + 7)/8;
        byte[] mpInt = new byte[byteLength];
	System.arraycopy(byteArray, offset, 
	    mpInt, 0,
	    byteLength);
        return mpInt;
    }

    //====================================================================
    // INSTANCE VARIABLES

    private Cipher _cipher = null;
    private GZInflater _inflater = null;
    private byte[] uncompressed_bytes = new byte[4096];
}
