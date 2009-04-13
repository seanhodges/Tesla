/* class SSHOutputStream
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
import java.security.*;
import com.jcraft.jzlib.*;
import de.mud.ssh.Cipher;

/**
 * This class allows the application to write SSH packets to a network
 * socket. The writePacket method compresses and encrypts the packet 
 * if appropriate.
 */
public class SSHOutputStream 
    extends DataOutputStream 
    implements IPacketConstants
{
    /** Constructor
     */
    public SSHOutputStream(OutputStream stream) 
    {
	super(stream);
	try {
	    _md5 = MessageDigest.getInstance("MD5");
	}
	catch (NoSuchAlgorithmException e) {
	    // Should never happen
	    System.err.println("MD5 algorithm not supported");
	}
    }

    /**
     */
    public void writePacket(Packet packet_)
	throws IOException
    {
	// Get the [ packet-type + binary data ] array
	byte[] data = packet_.getData();

	/* Compress the [ packet-type + data ] if compression is
	 * turned on.
	 */
	if (_deflater != null) {
	    _deflater.setInput(data);
	    int nbytes = _deflater.deflate(compressed_bytes, GZDeflater.Z_PARTIAL_FLUSH);
	    data = new byte[nbytes];
	    System.arraycopy(compressed_bytes, 0, data, 0, nbytes);
	}
	super.writeInt(data.length + 4);

	// Compute the amount of padding required
	int padding_length = (8 - ((data.length + 4) % 8));
	int block_length = (((data.length + 4) / 8) + 1) * 8;
	byte[] block = new byte[block_length];

	// Fill in the padding
	for (int i=0; i<padding_length; i++) {
	    if (_cipher == null)
		block[i] = 0;
	    else
		block[i] = SSHMisc.getNonZeroRandomByte(_md5);
	}

	// Fill in the data
	for (int i=0; i<data.length; i++) {
	    block[padding_length + i] = data[i];
	}

	/* Compute the CRC of [ Padding, Packet Type, Data ]
	 */
	int crc_offset = block.length - 4;
	long crc = SSHMisc.crc32(block, crc_offset);

	/* Fill in the CRC
	 */
	insertInteger((int) crc, crc_offset, block);

	/* We now encrypt the block if necessary.
	 */
	if (_cipher != null) {
	    block = _cipher.encrypt(block);
	}

	super.write(block);
	super.flush();
    }

    /** Set the cipher algorithm (this method must be called after 
     * the session key has been exchanged and the cipher type has 
     * been negotiated).
     */
    public void setCipher(Cipher cipher_) {
	_cipher = cipher_;
    }

    /** Set the compression level (by default compression is off). 
     * This method is called after both side have agreed on the
     * compression level.
     */
    public void setCompression(int level_) {
	_deflater = new GZDeflater(level_);
    }

    /**
     * Encode a String as a byte-array for insertion into an SSH packet.
     * First 4 bytes are the length of the string, msb first (not
     * including the length itself).  The following "length" bytes are
     * the string value.  There are no terminating null characters.
     */
    public static byte[] string2Bytes(String str) {

        int length = str.length();
        byte[] value = new byte[4 + length];

        value[3] = (byte) ((length) & 0xff);
        value[2] = (byte) ((length>>8) & 0xff);
        value[1] = (byte) ((length>>16) & 0xff);
        value[0] = (byte) ((length>>24) & 0xff);

        byte [] strByte = str.getBytes();

        for (int i=0; i<length; i++) 
	    value[i+4] = strByte[i];
        return value;
    }

    /** Encode the specified integer into a byte array as four consecutive
     * bytes, msb first. 
     * @param value_ the integer to be encoded.
     * @param offset_ the offset where the first byte must be stored. This
     * offset is NOT updated by the method (Java can't modify method
     * parameters).
     * @param bytes_ the byte array into which the integer must be inserted.
     */
    public static void insertInteger(int value_, int offset_, byte[] bytes_) {
	bytes_[offset_++] = (byte) ((value_ >> 24) & 0xff);
	bytes_[offset_++] = (byte) ((value_ >> 16) & 0xff);
	bytes_[offset_++] = (byte) ((value_ >> 8) & 0xff);
	bytes_[offset_++] = (byte) (value_ & 0xff);
    }

    //====================================================================
    // INSTANCE VARIABLES

    private Cipher _cipher = null;
    private GZDeflater _deflater = null;
    private byte[] compressed_bytes = new byte[4096];
    private MessageDigest _md5;
}
