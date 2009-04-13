/* class SMSG_FAILURE
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
 * Sent by the server in response to a failed authentication 
 * operation to indicate that the user has not yet been successfuly
 * authenticated, and in response to a failed preparatory operation.
 * It is also sent in response to an authentication or preparatory operation
 * that is not recognised or supported.
 */
class SMSG_FAILURE
    extends Packet
{

    SMSG_FAILURE(byte[] data_) {
	super(data_);
    }
}
