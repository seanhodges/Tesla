/*
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
 * For use on systems such as Linux which provide a "/dev/random"
 * device; obtains truly random bits from "/dev/random" for use
 * as a session key.
 */
public class DevRandom
    implements ITrueRandom
{
    /** Constructor
     */
    public DevRandom() {
	try {
	    File dev_random = new File("/dev/random");
	    _in = new FileInputStream(dev_random);
	}
	catch (IOException e) {
	    throw new RuntimeException("Cannot open /dev/random");
	}
    }

    /** Fill the specified byte-array with random bits.
     */
    public void getRandomBytes(byte[] bytes_) {
	try {
	    for (int i=0; i<bytes_.length; i++) {
		int random = _in.read();
		if (-1 == random)
		    throw new RuntimeException("/dev/random: End of File");
		else
		    bytes_[i] = (byte) random;
	    }
	}
	catch (IOException e) {
	    throw new RuntimeException("/dev/random: " + e.getMessage());
	}
	    
    }

    //====================================================================
    // INSTANCE VARIABLES
    FileInputStream _in;
}
