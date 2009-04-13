/* class CMSG_AUTH_RSA_RESPONSE
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
 * The client sends this message in response to an 
 * SSH_SMSG_AUTH_RSA_CHALLENGE. The client decrypts the challenge
 * (contained in the SSH_SMSG_AUTH_RSA_CHALLENGE) using its private key,
 * concatenates it with the session id, and computes an MD5 checksum
 * of the resulting 48 bytes. The MD5 output is returned as 16 bytes
 * in this message.
 */
class CMSG_AUTH_RSA_RESPONSE
    extends Packet
{
    /** Constructor.
     */
    CMSG_AUTH_RSA_RESPONSE(byte[] md5_checksum_) {
	super(SSH_CMSG_AUTH_RSA_RESPONSE, md5_checksum_);
    }
}
