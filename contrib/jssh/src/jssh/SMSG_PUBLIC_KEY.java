/* class SMSG_PUBLIC_KEY
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
 * Sent as the first message by the server. This message gives the server's:
 * <ul>
 *<li>host key
 *<li>server key
 *<li>protocol flags (intended for compatible protocol
 * extension)
 *<li>supported_ciphers_mask (which is the bitwise OR of (1 &lt;&lt;
 * cipher_number), where &lt;&lt; is the left-shift operator, for all the 
 * supported ciphers)
 *<li>supported_authentication_mask (which is the bitwise OR of (1 &lt;&lt;
 * authentication_type) for all supported authentication types).
 *</ul>
 */
public class SMSG_PUBLIC_KEY
    extends Packet
{
    /** Use this constructor when receiving a SMSG_PUBLIC_KEY packet
     * on the network.
     */
    SMSG_PUBLIC_KEY(byte[] data_) {
	super(data_);

	int offset = 1;   // skip packet-type byte

	for (int i=0; i<8; i++)
	    _anti_spoofing_cookie[i] = _data[offset++];

	for (int i=0; i<4; i++)
	    _server_key_bits[i] = _data[offset++];

	_server_key_public_exponent = SSHInputStream.getMpInt(offset, _data);
	offset += _server_key_public_exponent.length + 2;

	_server_key_public_modulus = SSHInputStream.getMpInt(offset, _data);
	offset += _server_key_public_modulus.length + 2;

	for (int i=0; i<4; i++) 
	    _host_key_bits[i] = _data[offset++];

	_host_key_public_exponent = SSHInputStream.getMpInt(offset, _data);
	offset += _host_key_public_exponent.length + 2;

	_host_key_public_modulus = SSHInputStream.getMpInt(offset, _data);
	offset += _host_key_public_modulus.length + 2;

	for (int i=0;i<4;i++) 
	    _protocol_flags[i] = _data[offset++];

	for (int i=0;i<4;i++) 
	    _supported_ciphers_mask[i] = _data[offset++];

	for (int i=0;i<4;i++) 
	    _supported_authentications_mask[i] = _data[offset++];
    }

    public byte[] getAntiSpoofingCookie() {
	return _anti_spoofing_cookie;
    }

    public byte[] getServerKeyBits() {
	return _server_key_bits;
    }

    public byte[] getServerKeyPublicExponent() {
	return _server_key_public_exponent;
    }

    public byte[] getServerKeyPublicModulus() {
	return _server_key_public_modulus;
    }

    public byte[] getHostKeyPublicExponent() {
	return _host_key_public_exponent;
    }

    public byte[] getHostKeyPublicModulus() {
	return _host_key_public_modulus;
    }

    public byte[] getProtocolFlags() {
	return _protocol_flags;
    }

    public byte[] getSupportedCiphersMask() {
	return _supported_ciphers_mask;
    }

    public byte[] getSupportedAuthenticationsMask() {
	return _supported_authentications_mask;
    }

    //====================================================================
    // INSTANCE VARIABLES

    private byte[] _anti_spoofing_cookie = new byte[8];

    private byte[] _server_key_bits = new byte[4];

    private byte[] _server_key_public_exponent;	    // mp-int

    private byte[] _server_key_public_modulus;	    // mp-int

    private byte[] _host_key_bits = new byte[4];

    private byte[] _host_key_public_exponent;	    // mp-int

    private byte[] _host_key_public_modulus;	    // mp-int

    private byte[] _protocol_flags = new byte[4];

    private byte[] _supported_ciphers_mask = new byte[4];

    private byte[] _supported_authentications_mask = new byte[4];
}
