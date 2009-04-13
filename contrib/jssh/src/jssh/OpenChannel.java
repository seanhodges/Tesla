/* class OpenChannel
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
 * This class represents an open SSH channel.
 */
class OpenChannel
    implements Runnable, IPacketConstants
{
    /** Use this constructor for a channel that implements remote-to-local
     * port-forwarding.
     */
    OpenChannel(IProtocolHandler handler_, String hostname_, 
	    int localport_, int remoteChannelNumber_) 
    {
	_handler = handler_;
	_hostname = hostname_;
	_localport = localport_;
	_remoteChannelNumber = remoteChannelNumber_;
    }

    /** Use this constructor for a channel that implements local-to-remote
     * port-forwarding. We have to send a SSH_MSG_PORT_OPEN to the remote 
     * side, containing the specified hostname and port,  and wait
     * for a SSH_MSG_CHANNEL_OPEN_CONFIRMATION in reply.
     */
    OpenChannel(IProtocolHandler handler_, Socket socket_,
	    String hostname_, int remoteport_) 
    {
	_handler = handler_;
	_socket = socket_;
	_hostname = hostname_;
	_remoteport = remoteport_;
    }

    void setLocalChannelNumber(int id_) {
	_localChannelNumber = id_;
    }

    int getLocalChannelNumber() {
	return _localChannelNumber;
    }

    int getRemoteChannelNumber() {
	return _remoteChannelNumber;
    }

    String getHostname() {
	return _hostname;
    }

    /** Enqueue an SSH packet onto the input queue of this channel.
     */
    void enqueue(Packet packet_) {
	_queue.enqueue(packet_);
    }

    public void run() {
	if (_socket != null) {
	    /* This is a local-to-remote channel, and we have received an 
	     * incoming TCP connection.
	     */
	    MSG_PORT_OPEN open_packet = 
		    new MSG_PORT_OPEN(_localChannelNumber, _hostname, _remoteport);
	    _handler.enqueueToRemote(open_packet);

	    // Wait for a reply from the remote side.
	    Packet reply = _queue.getNextPacket();
	    if (reply.getPacketType() != SSH_MSG_CHANNEL_OPEN_CONFIRMATION) {
		try { _socket.close(); } catch (IOException e) {}
		return;
	    }

	    MSG_CHANNEL_OPEN_CONFIRMATION confirm =
		(MSG_CHANNEL_OPEN_CONFIRMATION) reply;
	    _remoteChannelNumber = confirm.getLocalChannelNumber();
	}
	else {
	    /* This is a remote-to-local channel, and we have received a
	     * SSH_MSG_PORT_OPEN.
	     */
	    try {
		InetAddress address = InetAddress.getByName(_hostname);
		_socket = new Socket(address, _localport);
	    }
	    catch (IOException e) {
		MSG_CHANNEL_OPEN_FAILURE reply = 
		    new MSG_CHANNEL_OPEN_FAILURE(_remoteChannelNumber);
		_handler.enqueueToRemote(reply);
		return;
	    }

	    _handler.registerOpenChannel(this);
	    MSG_CHANNEL_OPEN_CONFIRMATION reply = 
		new MSG_CHANNEL_OPEN_CONFIRMATION(
		    _remoteChannelNumber, getLocalChannelNumber());
	    _handler.enqueueToRemote(reply);
	}

	try {
	    ChannelReader channel_reader = 
		new ChannelReader(_socket.getInputStream());

	    /* We don't want the application to exit until this thread 
	     * terminates.
	     */
	    channel_reader.setDaemon(false);
	    channel_reader.start();

	    OutputStream out = _socket.getOutputStream();

	    for (;;) {
		Packet packet = _queue.getNextPacket();

		int type = packet.getPacketType();
		if (type == SSH_MSG_CHANNEL_DATA) {
		    MSG_CHANNEL_DATA data_packet = (MSG_CHANNEL_DATA) packet;
		    out.write(data_packet.getChannelData());
		}
		else if (type == SSH_MSG_CHANNEL_CLOSE) {
		    _socket.shutdownOutput();
		    _socket.close();

		    MSG_CHANNEL_CLOSE_CONFIRMATION confirmation =
			new MSG_CHANNEL_CLOSE_CONFIRMATION(_remoteChannelNumber);
		    _handler.enqueueToRemote(confirmation);
		    break;		    // causes thread to terminate
		}
		else
		    break;
	    }
	}
	catch (IOException e) {
	    MSG_CHANNEL_CLOSE close_packet =
		new MSG_CHANNEL_CLOSE(_remoteChannelNumber);
	    _handler.enqueueToRemote(close_packet);

	    try { _socket.close(); } catch (Exception e2) {}
	}

	// We have received a packet other than SSH_MSG_CHANNEL_DATA.
	_handler.removeOpenChannel(this);
	return;	    // causes the thread to terminate.
    }

    /** A nonstatic inner class that runs in its own thread, reading data
     * from the network and forwarding it to the remote side via the 
     * encrypted channel.
     */
    private class ChannelReader extends Thread
    {
	/** Constructor
	 */
	ChannelReader(InputStream in_) { 
	    _in = in_;
	}

	/** Implements the Runnable interface
	 */
	public void run() {
	    byte[] buf = new byte[1024];
	    int n;

	    for (;;) {
		try {
		    n = _in.read(buf);
		}
		catch (IOException e) {
		    n = -1;
		}

		if (n == -1) {
		    // EOF or IOException encountered on the input stream.
		    MSG_CHANNEL_CLOSE close_packet =
			    new MSG_CHANNEL_CLOSE(_remoteChannelNumber);
		    _handler.enqueueToRemote(close_packet);
		    break;  // causes run() method to exit.
		}
		else {
		    MSG_CHANNEL_DATA data_packet = 
			new MSG_CHANNEL_DATA(_remoteChannelNumber, buf, n);
		    _handler.enqueueToRemote(data_packet);
		}
	    }
	}

	private InputStream _in;
    }

    //====================================================================
    // INSTANCE VARIABLES

    private IProtocolHandler _handler;

    // input queue for this channel.
    private PacketQueue _queue = new PacketQueue();

    private int _localChannelNumber;

    private int _remoteChannelNumber;

    private Socket _socket;

    private String _hostname = null;

    private int _remoteport;

    private int _localport;
}
