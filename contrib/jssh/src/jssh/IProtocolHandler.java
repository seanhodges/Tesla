/* IProtocolHandler.java
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

/**
 * This interface specifies methods that must be provided by
 * the SSH protocol handler in order to handle received packets.
 * It is designed generically so that it can be used for both
 * client and server protocol handlers.
 */
public interface IProtocolHandler
{
    /** Enqueues a packet to the SSH server.
     */
    public void enqueueToRemote(Packet packet_);

    /** Enqueues a packet to the STDOUT stream.
     */
    public void enqueueToStdout(Packet packet_);

    /** Registers an open SSH channel (encrypted tunnel).
     */
    public void registerOpenChannel(OpenChannel channel_);

    /** Deregisters an open SSH chanel.
     */
    public void removeOpenChannel(OpenChannel channel_);

    /** Finds the open channel with the specified channel number.
     */
    public OpenChannel findOpenChannel(int channel_number_);

    /** This method is called when a SSH_MSG_PORT_OPEN message
     * is received; it returns true if the port open request matches
     * one of the local port forwardings specified by the user.
     */
    public boolean isPortOpenAllowed(String hostname_, int port_);
}
