/* class CMSG_SESSION_KEY
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

import java.security.*;
import java.math.BigInteger;	//used to implement RSA.
import de.mud.ssh.Cipher;

/**
 * This message is sent by the client as the first message in the session. It
 * selects the cipher to use, and sends the encrypted session key to the
 * server. The anti_spoofing_cookie must be the same bytes that were sent by
 * the server. Protocol_flags is intended for negotiating compatible protocol
 * extensions.
 */
class CMSG_SESSION_KEY
    extends Packet
{
    /** Construct the SSH_CMSG_SESSION_KEY packet.
     *
     * @param trueRandom_ an object that implements the ITrueRandom interface
     * and supplies truly random bits (not pseudo-random bits).
     *
     * @param keypacket_ the SMSG_PUBLIC_KEY packet that we received from the
     * server.
     */
    CMSG_SESSION_KEY(ITrueRandom trueRandom_, SMSG_PUBLIC_KEY keypacket_) 
	throws SSHSetupException
    {
	super();

	/* Initialize the message digest generator.
	 */
	try {
	    _md5 = MessageDigest.getInstance("MD5");
	}
	catch (NoSuchAlgorithmException e) {
	    throw new SSHSetupException("MD5 hash algorithm not supported");
	}

	byte[] anti_spoofing_cookie = keypacket_.getAntiSpoofingCookie();
	byte[] server_key_public_modulus = keypacket_.getServerKeyPublicModulus();
	byte[] host_key_public_modulus = keypacket_.getHostKeyPublicModulus();
	byte[] server_key_public_exponent = keypacket_.getServerKeyPublicExponent();
	byte[] host_key_public_exponent = keypacket_.getHostKeyPublicExponent();
	byte[] supported_ciphers_mask = keypacket_.getSupportedCiphersMask();

        /* create the session id (for protocol v1.5)
         *	_session_id = md5(hostkey->n || servkey->n || cookie) 
	 */
        _session_id = new byte[
	    host_key_public_modulus.length + 
	    server_key_public_modulus.length +
	    anti_spoofing_cookie.length];

        System.arraycopy(host_key_public_modulus, 0, 
	    _session_id, 0, 
	    host_key_public_modulus.length);

        System.arraycopy(server_key_public_modulus, 0,
	    _session_id, host_key_public_modulus.length,
	    server_key_public_modulus.length);

        System.arraycopy(anti_spoofing_cookie, 0,
	    _session_id, host_key_public_modulus.length + server_key_public_modulus.length,
	    anti_spoofing_cookie.length);

	_md5.reset();
	_session_id = _md5.digest(_session_id);

        // SSH_CMSG_SESSION_KEY : Sent by the client
        //    1 byte       cipher_type (must be one of the supported values)
        //    8 bytes      anti_spoofing_cookie (must match data sent by the server)
        //    mp-int       double-encrypted session key (uses the session-id)
        //    32-bit int   protocol_flags
        if ((supported_ciphers_mask[3] & (byte)(1<< Cipher.SSH_CIPHER_BLOWFISH))!=0) {
            _cipher_type = Cipher.SSH_CIPHER_BLOWFISH;
            _cipher_name = "Blowfish";
        }
        else {
            if ((supported_ciphers_mask[3] & (1 << Cipher.SSH_CIPHER_IDEA)) != 0) {
                _cipher_type = Cipher.SSH_CIPHER_IDEA;
                _cipher_name = "IDEA";
            }
            else {
                if ((supported_ciphers_mask[3] & (1 << Cipher.SSH_CIPHER_3DES)) != 0) {
                    _cipher_type = Cipher.SSH_CIPHER_3DES;
                    _cipher_name = "DES3";
                }
                else {
                    throw new SSHSetupException(
			"server does not support IDEA, BlowFish or 3DES, " +
			"cipher mask is 0x" + 
			Integer.toString(supported_ciphers_mask[3], 16));
                }
            }
        }

        // anti_spoofing_cookie : the same as received
        // double_encrypted_session_key :
        //  32 bytes of random bits
        //	XOR the first 16 bytes with the session-id.
        //	Encrypt with the server_key_public (small) then the 
	//	    host_key_public (big) using RSA.

        //32 bytes of random bits
        byte[] random_bits1 = new byte[16];
	byte[] random_bits2 = new byte[16];

	/* System.currentTimeMillis() returns the number of milliseconds 
	 * since January 1, 1970, 00:00:00 GMT.
         * Math.random()   a pseudorandom double between 0.0 and 1.0.
	 */
//	_md5.reset();
//	String random_string = "" + Math.random() * System.currentTimeMillis();
//	random_bits1 = _md5.digest(random_string.getBytes());

	/*
        random_bits1 = md5.hash(SSHMisc.concatenate(md5.hash(_password + _login),
            random_bits1));
        random_bits2 = md5.hash(SSHMisc.concatenate(md5.hash(_password + _login),
            random_bits2));
	 */

//        java.security.SecureRandom random = 
//		new java.security.SecureRandom(random_bits1);
//        random.nextBytes(random_bits1);
//        random.nextBytes(random_bits2);
	trueRandom_.getRandomBytes(random_bits1);
	trueRandom_.getRandomBytes(random_bits2);

        _session_key  = SSHMisc.concatenate(random_bits1, random_bits2);

        /* XOR the first 16 bytes with the session-id created earlier.
	 */
        byte[] session_keyXored  = SSHMisc.XORByteArrays(random_bits1, _session_id);
        session_keyXored = SSHMisc.concatenate(session_keyXored, random_bits2);

	/* The protocol spec says that "the resulting string is then encrypted
	 * with the smaller key (one with the smaller modulus), and the result
	 * is then encrypted with the other key".
	 */
	byte[] encrypted_session_key;
	if (server_key_public_modulus.length < host_key_public_modulus.length) {
	    byte[] server_key_encrypted_data = RSAAlgorithm.publicKeyEncrypt(
		session_keyXored,
		server_key_public_exponent,
		server_key_public_modulus,
		_md5);

	    encrypted_session_key = RSAAlgorithm.publicKeyEncrypt(
		server_key_encrypted_data,
		host_key_public_exponent,
		host_key_public_modulus,
		_md5);
	}
	else {
	    byte[] server_key_encrypted_data = RSAAlgorithm.publicKeyEncrypt(
		session_keyXored,
		host_key_public_exponent,
		host_key_public_modulus,
		_md5);

	    encrypted_session_key = RSAAlgorithm.publicKeyEncrypt(
		server_key_encrypted_data,
		server_key_public_exponent,
		server_key_public_modulus,
		_md5);
	}

        // protocol_flags :protocol extension (page 18 of SSH Protocol 1.5)
        byte[] protocol_flags = new  byte[4];               //32-bit int
        protocol_flags [0] = protocol_flags [1] =
            protocol_flags [2] = protocol_flags [3] = 0;

        //set the data
        int block_length = 
	    1 +		// packet type
	    1 +		// cipher_type
            anti_spoofing_cookie.length +
            2 + encrypted_session_key.length +
            protocol_flags.length;

        super._data = new byte[block_length];

        int offset = 0;
        super._data[offset++] = (byte) SSH_CMSG_SESSION_KEY;
        super._data[offset++] = (byte) _cipher_type;

        for (int i=0; i<8; i++)
            super._data[offset++] = anti_spoofing_cookie[i];

        super._data[offset++] = (byte) (((8 * encrypted_session_key.length) >> 8) & 0xff);
        super._data[offset++] = (byte) ((8 * encrypted_session_key.length) & 0xff);

        for (int i = 0; i < encrypted_session_key.length; i++)
            super._data[offset++] = encrypted_session_key[i];

        for (int i=0; i<4; i++)
            super._data[offset++] = protocol_flags[i];
    }

    /** Returns the session key generated by the client.
     */
    public byte[] getSessionKey() { return _session_key; }

    /** Returns the session id generated by the client.
     */
    public byte[] getSessionID() { return _session_id; }

    /** Returns the name of the cipher negotiated with the server.
     */
    public String getCipherName() { return _cipher_name; }

    //====================================================================
    // INSTANCE VARIABLES

    private MessageDigest _md5;

    private byte _cipher_type;	    //encryption types

    private String _cipher_name = "";

    private byte[] _session_id;

    private byte[] _session_key;

}
