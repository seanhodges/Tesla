/* KnownHostsFile.java
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

import java.math.BigInteger;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class provides methods for checking whether a server's
 * public key is in a known_hosts file, and for adding a key
 * to the file.
 */
public class KnownHostsFile
    extends File
{
    /** Constructor
     * @param pathname_ the filename of the known_hosts file.
     */
    public KnownHostsFile(String pathname_) {
	super(pathname_);
    }

    /** Check that the server's host key is in the known_hosts file.
     * @param server_address_ the hostname of the SSH server.
     * @param host_key_public_modulus_ the host key public modulus.
     * @param host_key_public_exponent_ the host key public exponent.
     * @return the return status:<p>
     * <ul>
     * <li>HOST_KEY_OK if the host key was found in the known_hosts 
     * file and matched the received key.
     * <li>HOST_KEY_DIFFERS if the host key was found in the known_hosts
     * file but differed from the received key.
     * <li> HOST_KEY_NEW if the host key was not found in the known_hosts
     * file (or the known_hosts file did not exist).
     * </ul>
     */
    public int check_host_key(
	    String server_name_,
	    BigInteger received_modulus_,
	    BigInteger received_exponent_) 
	throws IOException
    {
	String line = null;
	BufferedReader reader = null;
	try {
	    reader = new BufferedReader(
		new FileReader(this));
	}
	catch (FileNotFoundException e) {
	    return HOST_KEY_NEW;
	}

	while ((line = reader.readLine()) != null) {
	    if (line.startsWith("#") || line.trim().length() == 0)
		continue;

	    StringTokenizer st = new StringTokenizer(line);
	    String name_token = st.nextToken();

	    StringTokenizer st2 = new StringTokenizer(name_token, ",");
	    while (st2.hasMoreTokens()) {
	    	String hostname = st2.nextToken();
		if (hostname.equals(server_name_)) {
		    reader.close();
		    String nbits = st.nextToken();
		    String exp_str = st.nextToken();
		    BigInteger expected_exponent = new BigInteger(exp_str);
		    if (received_exponent_.equals(expected_exponent) == false)
			return HOST_KEY_DIFFERS;

		    String mod_str = st.nextToken();
		    BigInteger expected_modulus = new BigInteger(mod_str);
		    if (received_modulus_.equals(expected_modulus) == false)
			return HOST_KEY_DIFFERS;

		    return HOST_KEY_OK;
		}
	    }	    // end inner while
	}	    // end outer while
	reader.close();
	return HOST_KEY_NEW;
    }

    /** Add the server's host key to this known_hosts file.
     * @param server_name_ the hostname of the SSH server.
     * @param host_key_public_modulus_ the host key public modulus.
     * @param host_key_public_exponent_ the host key public exponent.
     */
    public void add_host_key(
	    String server_name_,
	    BigInteger modulus_,
	    BigInteger exponent_)
	throws IOException
    {

	BufferedWriter writer = new BufferedWriter(
		new FileWriter(super.getAbsolutePath(), true));
	try {
	    InetAddress address = InetAddress.getByName(server_name_);
	    String dotted_address = address.getHostAddress();
	    writer.write(server_name_ + "," + dotted_address +
		" " + modulus_.bitLength() + " " + 
		exponent_ + " " + modulus_ + "\n");
	}
	catch (UnknownHostException e) {}   // never happens
	writer.close();
    }

    public static final int HOST_KEY_OK = 0;
    public static final int HOST_KEY_DIFFERS = 1;
    public static final int HOST_KEY_NEW = 2;
}
