/*
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

/**
 * This class manages a set of encrypted SSH channels.
 */
class ChannelManager
{
    ChannelManager() {
    }

    /**
     * Creates and registers a channel for local-to-remote port-forwarding.
     */
    synchronized void registerLocalChannel(Channel channel_)
    {
	_localChannels.add(channel_);
    }

    /** This method is called by an open channel when it has
     * succeeded in opening its TCP connection to the specified hostname
     * and port.
     */
    synchronized void registerOpenChannel(OpenChannel channel_) {
	channel_.setLocalChannelNumber(_nextLocalNumber++);
	_openChannels.add(channel_);
    }

    /** Find the local channel with the specified local ID.
     */
    synchronized OpenChannel findOpenChannel(int ID_) {
	Iterator iter = _openChannels.iterator();
	while (iter.hasNext()) {
	    OpenChannel chan = (OpenChannel) iter.next();
	    if (chan.getLocalChannelNumber() == ID_)
		return chan;
	}
	return null;	// couldn't find it.
    }

    /** Removes the channel with the specified local ID from the list
     * of open channels.
     */
    synchronized void removeOpenChannel(OpenChannel channel_) {
	_openChannels.remove(channel_);
    }
    
    /** Returns true if one or more forwarded connections are open; otherwise
     * false.
     */
    synchronized boolean channelsAreOpen() {
	return (_openChannels.size() != 0);
    }

    //====================================================================
    // INSTANCE VARIABLES

    /** This is a list of local-to-remote channels that are listening
     * on the specified local port for incoming TCP connections.
     */
    private ArrayList _localChannels = new ArrayList();

    /** This is a list of OpenChannels that are associated with open TCP
     * connections.
     */
    private ArrayList _openChannels = new ArrayList();

    private int _nextLocalNumber = 5;
}
