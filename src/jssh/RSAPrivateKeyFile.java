/* RSAPrivateKeyFile.java
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
import java.math.BigInteger;
import de.mud.ssh.*;
import java.security.*;

/**
 * This class encapsulates the properties and behaviour of an
 * SSH RSA private-key file. The file has the following structure:<p>
 *
 * <UL>
 * <LI> The null-terminated string "SSH PRIVATE KEY FILE FORMAT 1.1\n"
 * <LI> A single byte indicating the cipher used to encrypt the private key.
 * Possible values are: 0 for no encryption, and 3 for 3DES in CBC mode.
 * <LI> A 32-bit int ("for future extension").
 * <LI> Another 32-bit int indicating the number of bits in the key (ignored).
 * <LI> A multiprecision int (the public modulus)
 * <LI> A multiprecision int (the public exponent)
 * <LI> A comment string.
 * <LI> The remaining bytes of the file contain the private key, encrypted
 * (if the cipher type is nonzero) with the specified cipher.
 *</UL>
 */
public class RSAPrivateKeyFile
{
    /** Constructor
     */
    public RSAPrivateKeyFile(String filename_) 
	throws IOException
    {
	DataInputStream in = new DataInputStream(
				new FileInputStream(filename_));

	byte[] file_id = new byte[FILE_ID.length()];
	in.readFully(file_id);
	in.readByte();
	String id_string = new String(file_id);
	if (id_string.equals(FILE_ID) == false) {
	    throw new IOException("private key file corrupted");
	}

	_cipherType = in.readByte();
	if ( (_cipherType != Cipher.SSH_CIPHER_NONE) && 
		(_cipherType != Cipher.SSH_CIPHER_3DES))
	    throw new IOException("key encrypted with invalid cipher type!");

	in.readInt();	// reserved for future use...
	in.readInt();	// number of bits; ignore...

	byte[] n = readMpInt(in);
	byte[] e = readMpInt(in);
	_publicKey = new RSAPublicKey(n, e);

	_comment = readString(in);
	byte[] buf = new byte[1000];
	int offset = 0;
	while (true) {
	    int nbytes = in.read(buf, offset, buf.length - offset);
	    if (nbytes < 0)
		break;
	    offset += nbytes;
	}

	// Now we know how many bytes there are remaining in the file.
	_encrypted = new byte[offset];
	System.arraycopy(buf, 0,
	    _encrypted, 0, offset);

	in.close();
    }

    public int getCipherType() {
	return _cipherType;
    }

    public String getComment() {
	return _comment;
    }

    /** This method should be called if the key is not encrypted, ie
     * getCipherType() returns SSH_CIPHER_NONE;
     */
    public RSAPrivateKey getPrivateKey() 
	throws IOException
    {
	return _getPrivateKey(_encrypted);
    }

    /** This method should be called if the key is encrypted, ie if
     * getCipherType() returns SSH_CIPHER_3DES.
     */
    public RSAPrivateKey getPrivateKey(String passphrase_) 
	throws IOException
    {
	/* The private key is encrypted with 3DES.
	 */
	Cipher cipher = Cipher.getInstance("DES3");

	MessageDigest md5 = null;
	try { md5 = MessageDigest.getInstance("MD5"); }
	catch (NoSuchAlgorithmException e) {
	    throw new RuntimeException("MD5 hash not implemented!");
	}

	byte[] key = md5.digest(passphrase_.getBytes());
	cipher.setKey(key);
	byte[] decrypted = cipher.decrypt(_encrypted);
	return _getPrivateKey(decrypted);
    }

    /** Private method that extracts the private key from the decrypted
     * buffer.
     */
    private RSAPrivateKey _getPrivateKey(byte[] decrypted_) 
	throws IOException
    {

	DataInputStream in = new DataInputStream(
				new ByteArrayInputStream(decrypted_));

	byte[] checkbytes = new byte[4];
	in.readFully(checkbytes);
	if (checkbytes[0] != checkbytes[2] || checkbytes[1] != checkbytes[3])
	    throw new IOException("private key corrupted");

	byte[] d = readMpInt(in);
	byte[] u = readMpInt(in);
	byte[] p = readMpInt(in);
	byte[] q = readMpInt(in);
	in.close();

	return new RSAPrivateKey(_publicKey.getModulus(), d, _comment);
    }

    /** Read a multiprecision integer from the input stream
     */
    private byte[] readMpInt(DataInputStream stream_) 
	throws IOException
    {
	short bits = stream_.readShort();
	int bitlen = ((int) bits) & 0xffff;	// convert to unsigned int
	int bytelen = (bitlen + 7) / 8;
	byte[] mp_int = new byte[bytelen];
	stream_.readFully(mp_int);
	return mp_int;
    }

    /** Read a string from the input stream.
     */
    private String readString(DataInputStream stream_) 
	throws IOException
    {
	int len = stream_.readInt();
	byte[] bytes = new byte[len];
	stream_.readFully(bytes);
	String str = new String(bytes);
	return str;
    }

/*
    public static void main(String[] args) {
	try {
	    RSAPrivateKeyFile keyfile = 
		    new RSAPrivateKeyFile(System.getProperty("user.home") +
		    "/.ssh/identity");
	    RSAPrivateKey key = keyfile.getPrivateKey();
	    System.err.println(SSHMisc.debug(key.getModulus()));
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
*/

    //====================================================================
    private static final String FILE_ID = "SSH PRIVATE KEY FILE FORMAT 1.1\n";

    private byte _cipherType;
    private String _comment;
    private byte[] _encrypted;
    private RSAPublicKey _publicKey;
}
