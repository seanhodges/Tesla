/* GZInflater.java
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

import com.jcraft.jzlib.*;

/**
 * This class provides support for uncompression using the
 * GZIP compression algorithm. Unlike the java.util.zip.Inflater
 * class, it allows access to the Z_PARTIAL_FLUSH option and
 * can therefore be used for uncompressing SSH packets.
 * Actually, this class is just a wrapper around the "ZStream"
 * class developed by JCraft Inc.
 */
public class GZInflater
{
    /** Creates a new uncompressor.
     */
    public GZInflater() {
	_zstream.inflateInit();
    }

    /** Uncompresses bytes into the specified buffer, and returns the
     * number of bytes uncompressed.
     * @param b_ the buffer for the uncompressed bytes.
     * @return the number of uncompressed bytes.
     */
    public int inflate(byte[] b_, int flush_) {
	return inflate(b_, 0, b_.length, flush_);
    }

    /** Uncompresses bytes into the specified buffer, and returns the
     * number of bytes uncompressed.
     * @param b_ the buffer for the uncompressed bytes.
     * @param offset_ the start offset for the uncompressed data.
     * @param len_ the maximum number of uncompressed bytes (i.e. the 
     * space available in the output buffer).
     * @param flush_ an integer parameter specifying the flushing option.
     * The only supported value for this parameter is Z_PARTIAL_FLUSH.
     * @return the number of uncompressed bytes.
     */
    public int inflate(byte[] b_, int offset_, int len_, int flush_) {
	_zstream.next_out = b_;
	_zstream.next_out_index = offset_;
	_zstream.avail_out = len_;
	for (;;) {
	    int status = _zstream.inflate(flush_);
	    if (status == ZStream.Z_OK) {
		continue;
	    }
	    else if (status == ZStream.Z_BUF_ERROR) {
		break;
	    }
	    else {
		System.err.println("ERROR: inflate returned " + status);
		break;
	    }
	}
	return _zstream.next_out_index - offset_;
    }

    /** Returns true if the end of the compressed data stream has been
     * reached.
     */
    public boolean finished() {
	return (_status == ZStream.Z_STREAM_END);
    }

    /** Returns true if no data remains in the input buffer.
     */
    public boolean needsInput() {
	return (_zstream.avail_in == 0);
    }

    /** Sets input data for uncompression. Should be called whenever
     * needsInput() returns true, indicating that the input buffer is 
     * empty.
     */
    public void setInput(byte[] b_) {
	setInput(b_, 0, b_.length);
    }

    /** Sets input data for uncompression. Should be called whenever
     * needsInput() returns true, indicating that the input buffer is 
     * empty.
     */
    public void setInput(byte[] b_, int offset_, int len_) {
	_zstream.next_in = b_;
	_zstream.next_in_index = offset_;
	_zstream.avail_in = len_;
    }

    //====================================================================
    // INSTANCE VARIABLES

    private ZStream _zstream = new ZStream();
    private int _status;
    public static final int Z_PARTIAL_FLUSH = ZStream.Z_PARTIAL_FLUSH;
}
