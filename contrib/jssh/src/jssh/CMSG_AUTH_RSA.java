/* class CMSG_AUTH_RSA
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
 * The client sends this message containing the client's public key,
 * in order to initiate RSA authentication. If the server is willing
 * to accept authentication with the offered public key, it sends
 * an SSH_SMSG_AUTH_RSA_CHALLENGE containing random data encrypted
 * with the client's public key.
 */
class CMSG_AUTH_RSA
    extends Packet
{
    /** Constructor.
     */
    CMSG_AUTH_RSA(byte[] public_modulus_) {
	super();

	int packet_length =
	    1 +
	    2 +
	    public_modulus_.length;

	super._data = new byte[packet_length];

	int offset = 0;
	super._data[offset++] = SSH_CMSG_AUTH_RSA;
        super._data[offset++] = (byte) (((8 * public_modulus_.length) >> 8) & 0xff);
        super._data[offset++] = (byte) ((8 * public_modulus_.length) & 0xff);

        for (int i = 0; i < public_modulus_.length; i++)
            super._data[offset++] = public_modulus_[i];
    }
}
