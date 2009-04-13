/* Channel.java
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

import java.net.*;
import java.io.*;

/**
 * This class represents an SSH channel that implements local-to-remote
 * port-forwarding.
 * It runs in its own thread. 
 */
public class Channel
    extends Thread
{
    /**
     * Use this constructor to create a Channel that implements 
     * local-to-remote port forwarding.
     */
    public Channel(IProtocolHandler handler_, int localport_, 
	    String hostname_, int remoteport_) 
	throws IOException
    {
	_handler = handler_;
	_hostname = hostname_;
	_localport = localport_;
	_remoteport = remoteport_;

	_server_socket = new ServerSocket(_localport);
    }

    /** Implements the Runnable interface.
     */
    public void run() {
	for (;;) {
	    listenForConnection(_server_socket);
	}
    }

    /** Listens on the specified local port for an incoming TCP connection.
     * When it receives a connection, it queues a SSH_MSG_PORT_OPEN packet
     * to the remote side, then waits for the response (which should be a
     * SSH_MSG_CHANNEL_OPEN_CONFIRMATION or a SSH_MSG_CHANNEL_OPEN_FAILURE).
     * If it receives a confirmation, it waits for SSH_MSG_CHANNEL_DATA
     * packets and then calls the forwardData() method.
     */
    private void listenForConnection(ServerSocket s_) {
	Socket incoming = null;
	try {
	    incoming = s_.accept();
	}
	catch (IOException e) {
	    e.printStackTrace();    // should never happen
	}

	OpenChannel openchannel = new OpenChannel(_handler, incoming, 
	    _hostname, _remoteport);
	_handler.registerOpenChannel(openchannel);
	Thread channelThread = new Thread(openchannel);

	/* We don't want the application to exit until all open channels
	 * have closed, so set this to a non-daemon thread.
	 */
	channelThread.setDaemon(false);
	channelThread.start();
    }

    //====================================================================
    // INSTANCE VARIABLES

    private IProtocolHandler _handler;

    private String _hostname;

    private int _localport;

    private int _remoteport;

    private int _remoteChannelNumber;

    private ServerSocket _server_socket;
}
