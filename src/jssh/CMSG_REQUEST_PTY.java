/* class CMSG_REQUEST_PTY
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
 * This message is sent by the client to request that a pseudo-terminal 
 * device be allocated for this session. The user terminal type 
 * and terminal modes are supplied as arguments.
 */
class CMSG_REQUEST_PTY
    extends Packet
{

    /**
     * Construct a SSH_CMSG_REQUEST_PTY packet.
     * @param terminal_ the TERM environment variable.
     * @param rows_ the terminal height in rows.
     * @param columns_ the terminal width in columns.
     * @param modes_ the terminal modes, encoded as specified in
     * the SSH protocol 1.5 spec (page 30 onwards).
     */
    CMSG_REQUEST_PTY(String terminal_, 
	    int rows_, int columns_, byte[] modes_) 
    {
	super();

	byte[] terminal_type = SSHOutputStream.string2Bytes(terminal_);

	int block_length = 
	    1 +				    // packet-type byte
	    terminal_type.length +	    // terminal-type
	    4 +				    // number of rows
	    4 +				    // number of columns
	    4 +				    // width in pixels
	    4 +				    // height in pixels
	    modes_.length;

	super._data = new byte[block_length];

	int offset = 0;
	super._data[offset++] = SSH_CMSG_REQUEST_PTY;

	for (int i=0; i<terminal_type.length; i++)
	    super._data[offset++] = terminal_type[i];

	SSHOutputStream.insertInteger(rows_, offset, super._data);
	offset += 4;

	SSHOutputStream.insertInteger(columns_, offset, super._data);
	offset += 4;

	for (int i=0; i<8; i++)
	    super._data[offset++] = 0;	    // width & height in pixels

	for (int i=0; i<modes_.length; i++)
	    super._data[offset++] = modes_[i];
    }

    // The following opcodes are used for defining terminal modes.
    public static final int TTY_OP_END =    0;
    public static final int VINTR =	    1;
    public static final int VQUIT =	    2;
    public static final int VERASE =	    3;
    public static final int VKILL =	    4;
    public static final int VEOF =	    5;
    public static final int VEOL =	    6;
    public static final int VEOL2 =	    7;
    public static final int VSTART =	    8;
    public static final int VSTOP =	    9;
    public static final int VSUSP =	    10;
    public static final int VDSUSP =	    11;
    public static final int VREPRINT =	    12;
    public static final int VWERASE =	    13;
    public static final int VLNEXT =	    14;

    public static final int ICRNL =	    36;
    public static final int ISIG =	    50;
    public static final int ICANON =	    51;
    public static final int ECHO =	    53;
    public static final int ECHOE =	    54;
    public static final int ECHOK =	    55;
    public static final int ECHONL =	    56;
    public static final int OPOST =	    70;
    public static final int ONLCR =	    72;
    public static final int CS8 =	    91;
}
