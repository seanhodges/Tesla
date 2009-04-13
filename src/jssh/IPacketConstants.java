/* IPacketConstants.java
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

/** Defines the various types of SSH packets.
 */
public interface IPacketConstants
{
    //  The supported packet types and the corresponding message numbers are
    //	given in the following table.  Messages with _MSG_ in their name may
    //	be sent by either side.  Messages with _CMSG_ are only sent by the
    //  client, and messages with _SMSG_ only by the server.
    //
    public static final int SSH_MSG_DISCONNECT =	    1;
    public static final int SSH_SMSG_PUBLIC_KEY =	    2;
    public static final int SSH_CMSG_SESSION_KEY =	    3;
    public static final int SSH_CMSG_USER =		    4;
    public static final int SSH_CMSG_AUTH_RSA =		    6;
    public static final int SSH_SMSG_AUTH_RSA_CHALLENGE =   7;
    public static final int SSH_CMSG_AUTH_RSA_RESPONSE =    8;
    public static final int SSH_CMSG_AUTH_PASSWORD =	    9;
    public static final int SSH_CMSG_REQUEST_PTY =	    10;
    public static final int SSH_CMSG_EXEC_SHELL =	    12;
    public static final int SSH_CMSG_EXEC_CMD =		    13;
    public static final int SSH_SMSG_SUCCESS =		    14;
    public static final int SSH_SMSG_FAILURE =		    15;
    public static final int SSH_CMSG_STDIN_DATA =	    16;
    public static final int SSH_SMSG_STDOUT_DATA =	    17;
    public static final int SSH_SMSG_STDERR_DATA =	    18;
    public static final int SSH_CMSG_EOF =		    19;
    public static final int SSH_SMSG_EXITSTATUS =	    20;
    public static final int SSH_MSG_CHANNEL_OPEN_CONFIRMATION = 21;
    public static final int SSH_MSG_CHANNEL_OPEN_FAILURE =  22;
    public static final int SSH_MSG_CHANNEL_DATA =	    23;
    public static final int SSH_MSG_CHANNEL_CLOSE =	    24;
    public static final int SSH_MSG_CHANNEL_CLOSE_CONFIRMATION = 25;
    public static final int SSH_CMSG_PORT_FORWARD_REQUEST = 28;
    public static final int SSH_MSG_PORT_OPEN =		    29;
    public static final int SSH_MSG_IGNORE =		    32;
    public static final int SSH_CMSG_EXIT_CONFIRMATION =    33;
    public static final int SSH_MSG_DEBUG =		    36;
    public static final int SSH_CMSG_REQUEST_COMPRESSION =  37;
}
