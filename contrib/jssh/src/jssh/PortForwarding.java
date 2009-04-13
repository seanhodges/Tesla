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

/** This class encapsulates the specifications of an SSH
 * tunnel.
 */
public class PortForwarding
{
    public PortForwarding(int listenport_, String hostname_,
	int hostport_) {
	_listenport = listenport_;
	_hostname = hostname_;
	_hostport = hostport_;
    }

    int getListenPort() {
	return _listenport;
    }

    String getHostname() {
	return _hostname;
    }

    int getHostPort() {
	return _hostport;
    }

    //====================================================================
    // INSTANCE VARIABLES

    private int _listenport;
    private String _hostname;
    private int _hostport;
}
