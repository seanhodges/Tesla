/* class CMSG_AUTH_PASSWORD
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
 * The client sends this message containing the password. Note that
 * the password is encrypted by the packet protocol.
 */
class CMSG_AUTH_PASSWORD
    extends Packet
{
    /** Constructor.
     */
    CMSG_AUTH_PASSWORD(String password_) {
	super(SSH_CMSG_AUTH_PASSWORD, SSHOutputStream.string2Bytes(password_));
    }
}
