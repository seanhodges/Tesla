/* class STDOUT_InputStream
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
 * This class receives SSH_SMSG_STDOUT_DATA packets on its input queue,
 * and makes the received data available via an InputStream interface.
 */
public class STDOUT_InputStream 
    extends InputStream 
    implements IPacketConstants
{
    /** Constructor
     */
    public STDOUT_InputStream() {
	super();
    }

    /**
     * Reads the next byte of data from this input stream.
     * @return the next byte of data, or -1 if the end of the stream
     * is reached.
     */
    public int read()
	throws IOException
    {
	if (_outputBytesLeft == 0) {
	    _refillOutputBuffer();
	    if (_outputBytesLeft == -1)
		return -1;
	}
	int retval = (int) _outputBuf[_outputOffset++];
	_outputBytesLeft--;
        return (retval & 0xff);
    }

    /**
     * Reads some number of bytes from this input stream and stores
     * them in the array b_.
     * @return the number of bytes read into the buffer, or -1 if there 
     * was no more data because the end of the stream was detected.
     */
    public int read(byte[] b_)
	throws IOException
    {
	return read(b_, 0, b_.length);
    }

    /**
     * Reads up to <code>len_</code> bytes of data from this input stream
     * into the specified array, starting at the specified offset.
     * @return the number of bytes read into the buffer, or -1 if there 
     * was no more data because the end of the stream was detected.
     */
    public int read(byte[] b_, int offset_, int len_) 
	throws IOException
    {
	if (_outputBytesLeft == 0) {
	    _refillOutputBuffer();
	    if (_outputBytesLeft == -1)
		return -1;
	}
	int n = (len_ < _outputBytesLeft) ? len_ : _outputBytesLeft;

	System.arraycopy(_outputBuf, _outputOffset, b_, offset_, n);
	_outputOffset += n;
	_outputBytesLeft -= n;
	return n;
    }

    /**
     */
    public void enqueue(Packet packet_) {
	_queue.enqueue(packet_);
    }

    /** This private method is called when the output buffer is
     * empty. It blocks until there is data available to refill the
     * output buffer.
     */
    private void _refillOutputBuffer() 
	throws IOException
    {
	// Blocks until data is available.
	Packet packet = _queue.getNextPacket();
	if (packet == null) {
	    /* The thread that was reading from the packet-queue was 
	     * interrupted.
	     */
	    throw new EOFException();
	}

	int type = packet.getPacketType();
	if (type == SSH_SMSG_STDOUT_DATA) {
	    SMSG_STDOUT_DATA stdout_packet = (SMSG_STDOUT_DATA) packet;
	    _outputBuf = stdout_packet.getStdoutData();
	    _outputOffset = 0;
	    _outputBytesLeft = _outputBuf.length;
	}
	else if (type == SSH_SMSG_STDERR_DATA) {
	    SMSG_STDERR_DATA stderr_packet = (SMSG_STDERR_DATA) packet;
	    _outputBuf = stderr_packet.getStderrData().getBytes();
	    _outputOffset = 0;
	    _outputBytesLeft = _outputBuf.length;
	}
	else if (type == SSH_MSG_DISCONNECT) {
	    MSG_DISCONNECT disconnect_packet = (MSG_DISCONNECT) packet;
	    throw new IOException(disconnect_packet.getCause());
	}
	else {
	    throw new RuntimeException(
		"STDOUT_InputStream received unexpected packet type "
		+ type);
	}
    }

    //====================================================================
    // INSTANCE VARIABLES

    private PacketQueue _queue = new PacketQueue();
    private byte[] _outputBuf;
    private int _outputOffset = 0;
    private int _outputBytesLeft = 0;

}
