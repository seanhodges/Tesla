/* STDIN_OutputStream.java
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

import java.io.*;

/**
 * This class provides an "OutputStream" interface for writing
 * keystrokes from the terminal keyboard, and puts them onto
 * a queue from which they are read by the SSH protocol handler.
 */
public class STDIN_OutputStream
    extends OutputStream
{
    public STDIN_OutputStream(IProtocolHandler handler_) {
	_handler = handler_;
    }

    public void write(int b) 
    {
	byte[] data = new byte[1];
	data[0] = (byte) b;
	write(data);
    }

    public void write(byte[] data_) 
    {
	_handler.enqueueToRemote(new CMSG_STDIN_DATA(data_));
    }

    /** Closes the output stream by enqueuing an SSH_CMSG_EOF packet.
     */
    public void close()
    {
	_handler.enqueueToRemote(new CMSG_EOF());
    }

    //====================================================================
    // INSTANCE VARIABLES

    private IProtocolHandler _handler;
}
