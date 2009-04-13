/* GZDeflater.java
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
 * This class provides support for compression using the
 * GZIP compression algorithm. Unlike the java.util.zip.Deflater
 * class, it allows access to the Z_PARTIAL_FLUSH option and
 * can therefore be used for compressing SSH packets.
 * Actually, this class is just a wrapper around the "ZStream"
 * class developed by JCraft Inc.
 */
public class GZDeflater
{
    /** Creates a new compressor.
     */
    public GZDeflater(int level_) {
	_zstream.deflateInit(level_);
    }

    /** Compresses bytes into the specified buffer, and returns the
     * number of compressed bytes.
     * @param b_ the buffer for the compressed bytes.
     * @param flush_ the flushing option (the only supported value
     * for this parameter is Z_PARTIAL_FLUSH).
     * @return the number of compressed bytes.
     */
    public int deflate(byte[] b_, int flush_) {
	return deflate(b_, 0, b_.length, flush_);
    }

    /** Compresses bytes into the specified buffer, and returns the
     * number of compressed bytes.
     * @param b_ the buffer for the compressed bytes.
     * @param offset_ the offset for the next compressed byte.
     * @param len_ the maximum number of compressed bytes (i.e. the 
     * space available in the output buffer).
     * @param flush_ the flushing option (the only supported value
     * for this parameter is Z_PARTIAL_FLUSH).
     * @return the number of compressed bytes.
     */
    public int deflate(byte[] b_, int offset_, int len_, int flush_) {
	_zstream.next_out = b_;
	_zstream.next_out_index = offset_;
	_zstream.avail_out = len_;
	do {
	    int status = _zstream.deflate(ZStream.Z_PARTIAL_FLUSH);
	    switch (status) {
		case ZStream.Z_OK:
		    break;

		default:
		    System.err.println("deflate returned " + status);
		    break;
	    }
	} while (_zstream.avail_out == 0);

	return _zstream.next_out_index - offset_;
    }

    /** When called, indicates that compression should end with the
     * current contents of the input buffer.
     */
    public void finish() {
	_status = _zstream.deflateEnd();
    }

    /** Returns true if the end of the compressed data output stream has been
     * reached.
     */
    public boolean finished() {
	return (_status == ZStream.Z_STREAM_END);
    }

    /** Returns true if the input buffer is empty and setInput() should
     * be called to provide more input.
     */
    public boolean needsInput() {
	return (_zstream.avail_in == 0);
    }

    /** Sets input data for compression
     * @param b_ the array of data bytes to be compressed.
     */
    public void setInput(byte[] b_) {
	_zstream.next_in = b_;
	_zstream.next_in_index = 0;
	_zstream.avail_in = b_.length;
    }

    /** Sets input data for compression
     * @param b_ the array of data bytes to be compressed.
     * @param offset_ the offset of the first byte to be compressed.
     * @param len_ the number of bytes to be compressed.
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
