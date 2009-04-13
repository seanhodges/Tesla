/* Options.java
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

import java.util.*;
import java.awt.Dimension;

/** This class encapsulates all the options that can be specified
 * on the command-line for the SSH client.
 */
public class Options
{
    public Options() {
    }

    public void setHostname(String hostname_) {
	_hostname = hostname_;
    }

    public String getHostname() {
	return _hostname;
    }

    public void setCommand(String command_) {
	_command = command_;
    }

    public String getCommand() {
	return _command;
    }

    public void setPort(int port_) {
	_port = port_;
    }

    public int getPort() {
	return _port;
    }

    public void setUser(String user_) {
	_user = user_;
    }

    public String getUser() {
	return _user;
    }

    public void setCompression(boolean flag_) {
	_compression = flag_;
    }

    /** Returns true if compression is enabled.
     */
    public boolean compressionEnabled() {
	return _compression;
    }

    /** Adds the specified local port forwarding to the list
     * of local ports to be forwarded to the server.
     */
    public void addLocalPortForwarding(PortForwarding pf_) {
	_localForwardings.add(pf_);
    }

    /** Adds the specified port-forwarding to the list of 
     * server ports that must be forwarded to the client.
     */
    public void addRemotePortForwarding(PortForwarding pf_) {
	_remoteForwardings.add(pf_);
    }

    /** Returns an iterator that allows the calling code to iterate
     * through the list of local port-forwardings.
     */
    public Iterator getLocalForwardings() {
	return _localForwardings.iterator();
    }

    /** Returns an iterator that allows the calling code to iterate
     * through the list of remote port-forwardings.
     */
    public Iterator getRemoteForwardings() {
	return _remoteForwardings.iterator();
    }

    /** This method is called when an SSH_MSG_PORT_OPEN message is received.
     * @param hostname_ the hostname contained in the SSH_MSG_PORT_OPEN.
     * @param port_ the port contained in the SSH_MSG_PORT_OPEN.
     */
    public boolean isPortOpenAllowed(String hostname_, int port_) {
	Iterator iter = _remoteForwardings.iterator();
	while (iter.hasNext()) {
	    PortForwarding pf = (PortForwarding) iter.next();
	    if (pf.getHostname().equals(hostname_) && 
		    pf.getHostPort() == port_)
		return true;
	}
	return false;
    }

    public void setDebug(boolean debug_) {
	_debug = debug_;
    }

    public boolean getDebug() {
	return _debug;
    }

    /** Set the terminal-type string that will be sent in the
     * SSH_CMSG_REQUEST_PTY packet.
     */
    public void setTerminalType(String term_) {
	_term = term_;
    }

    public String getTerminalType() {
	return _term;
    }

    public void setTerminalSize(int columns_, int rows_) {
	_termSize = new Dimension(columns_, rows_);
    }

    public Dimension getTerminalSize() {
	return _termSize;
    }

    public void setIdentityFile(String filename_) {
	_identity = filename_;
    }

    public String getIdentityFile() {
	return _identity;
    }

    /** Sets the interval (in seconds) between keepalive packets. In some
     * networks, firewalls or routers need to keep track of "sessions",
     * and can lose track of a session if they don't see a packet on that
     * session within some time period. Setting the keppalive timeout to 
     * a nonzero value causes a SSH_MSG_IGNORE packet to be sent if the
     * TCP connection has been idle for the specifid number of seconds.
     */
    public void setKeepaliveTimeout(int seconds_) {
	_keepaliveTimeout = seconds_;
    }

    public int getKeepaliveTimeout() {
	return _keepaliveTimeout;
    }

    //====================================================================
    // INSTANCE VARIABLES

    private String _hostname = null;

    // Optional command to be executed instead of an interactive shell.
    private String _command = null;

    private int _port = 22;

    private String _user = null;

    private boolean _compression = false;

    private ArrayList _localForwardings = new ArrayList();

    private ArrayList _remoteForwardings = new ArrayList();

    private boolean _debug = false;

    private String _term = "vt100";

    private Dimension _termSize = new Dimension(80, 24);

    private String _identity = 
	    System.getProperty("user.home") + "/.ssh/identity";

    /** By default, keepalives are disabled.
     */
    private int _keepaliveTimeout = 0;
}
