/* class SMSG_AUTH_RSA_CHALLENGE
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
 * Sent by the server in response to a SSH_CMSG_AUTH_RSA message from
 * the client. This message contains 32 8-bit random bytes, encrypted
 * with the client's public key.
 */
public class SMSG_AUTH_RSA_CHALLENGE
    extends Packet
{
    /** Use this constructor when receiving a SMSG_AUTH_RSA_CHALLENGE packet
     * on the network.
     */
    SMSG_AUTH_RSA_CHALLENGE(byte[] data_) {
	super(data_);
    }

    public byte[] getChallenge() {
	int offset = 1;   // skip packet-type byte
	return SSHInputStream.getMpInt(offset, super._data);
    }


    //====================================================================
    // INSTANCE VARIABLES
}
