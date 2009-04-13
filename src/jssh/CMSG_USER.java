/* class CMSG_USER
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
 * Sent by the client to begin authentication. Specifies the user name 
 * on the server to log in as. The server responds with SSH_SMSG_SUCCESS if no
 * authentication is needed for this user, or SSH_SMSG_FAILURE if
 * authentication is needed (or the user does not exist).
 */
class CMSG_USER
    extends Packet
{

    CMSG_USER(String username_) {
	super(SSH_CMSG_USER, SSHOutputStream.string2Bytes(username_));
    }
}
