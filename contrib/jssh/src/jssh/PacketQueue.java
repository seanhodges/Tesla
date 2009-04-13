/* class PacketQueue
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
 * This class queues SSH protocol packets.
 */
class PacketQueue
    extends java.util.LinkedList
{
    /**
     * Constructor 
     */
    PacketQueue() {
	super();
    }

    /**
     * Enqueue an Packet object onto the queue.
     */
    synchronized void enqueue(Packet packet_) {
	super.addLast(packet_);
	super.notifyAll();	    // wake up the dequeueing thread
    }

    /** Dequeue an Packet object off the queue; if the queue is empty,
     * wait until another thread enqueues an event onto it.
     * @return the next Packet from the queue; or <b>null</b> if the
     * thread was interrupted.
     */
    synchronized Packet getNextPacket() {

        /* If the queue is empty, block until another thread enqueues
         * an packet.
         */
        while (super.size() == 0) {
            try {
		wait();
	    }
	    catch (InterruptedException e) {
		return null;
	    }
        }      
	return (Packet) super.removeFirst();
    }
}
