/* ClientProtocolHandler.java
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
import java.net.*;
import java.util.*;
import java.math.BigInteger;
import java.security.*;
import de.mud.ssh.Cipher;

/**
 * This class performs all the SSH client protocol handling
 * on a specified network connection.<p>
 *
 * An example of how to use this protocol handler is provided in the
 * {@link SSHClient SSHClient} class.
 */
public class ClientProtocolHandler
    implements Runnable, IProtocolHandler, IPacketConstants
{
    public ClientProtocolHandler(Socket socket_, Options options_) 
	throws IOException
    {
	_socket = socket_;
	_instream = new BufferedInputStream(socket_.getInputStream());
	_outstream = new BufferedOutputStream(socket_.getOutputStream());
	_options = options_;
    }

    /** Enqueues a packet to the SSH server.
     */
    public void enqueueToRemote(Packet packet_) {
	_clientqueue.enqueue(packet_);
    }

    /** Enqueues a packet to the STDOUT.
     */
    public void enqueueToStdout(Packet packet_) {
	_stdout.enqueue(packet_);
    }

    /** Returns a reference to the InputStream object from which
     * data must be read (for displaying on the screen).
     */
    public STDOUT_InputStream getSTDOUT() {
	return _stdout;
    }

    /** Returns a reference to the OutputStream object to which
     * keyboard data must be written (for sending to the server).
     */
    public STDIN_OutputStream getSTDIN() {
	if (_stdin == null)
	    _stdin = new STDIN_OutputStream(this);
	return _stdin;
    }

    /** This method exchanges version identification strings with the
     * SSH server.
     * @exception SSHSetupException if the server's version string is
     * incompatible.
     */
    public void exchangeIdStrings() 
	throws IOException, SSHSetupException
    {
	if (_phase != PHASE_EXCHANGE_ID_STRINGS) {
	    throw new RuntimeException(
		"SSH protocol already in packet mode");
	}

	for (;;) {
	    int b = _instream.read();
	    if (b == '\n') {
		debug("Remote version: " + _id_string_received.toString());
		debug("Local version string: " + ID_STRING_SENT);

		_outstream.write((ID_STRING_SENT + "\n").getBytes());
		_outstream.flush();

		// Here we should check that the server version is compatible.
		if (_id_string_received.toString().startsWith("SSH-1") == false) {
		    throw new SSHSetupException("incompatible server version: " +
			_id_string_received.toString());
		}
		_phase = PHASE_RECEIVE_SERVER_KEY;
		return;
	    }
	    _id_string_received.append((char) b);
	}
    }

    /** Wait for the SSH_SMSG_PUBLIC_KEY packet from the server. 
     * This method must be called after <code>exchangeIdStrings()</code>
     * has returned successfully. 
     */
    public void receiveServerKey() 
	throws IOException, SSHSetupException, SSHProtocolException
    {
	if (_phase != PHASE_RECEIVE_SERVER_KEY) {
	    throw new RuntimeException(
		"SSH protocol not in packet mode yet");
	}

	_ssh_in = new SSHInputStream(_instream);
	_ssh_out = new SSHOutputStream(_outstream);

	debug("Waiting for server public key.");
	Packet packet = readNextPacket();
	if (packet.getPacketType() != SSH_SMSG_PUBLIC_KEY) {
	    throw new SSHProtocolException("Received packet type " + 
		packet.getPacketType() + ", expected SSH_SMSG_PUBLIC_KEY");
	}
	_server_public_key_packet = (SMSG_PUBLIC_KEY) packet;
	debug("Received server public key (" +
	    (8 * _server_public_key_packet.getServerKeyPublicModulus().length) +
	    " bits) and host key (" +
	    (8 * _server_public_key_packet.getHostKeyPublicModulus().length) +
	    " bits).");

	_phase = PHASE_SEND_SESSION_KEY;
	return;
    }

    /** Create and send an encrypted session key; this method must be 
     * called AFTER receiveServerKey() has returned successfully.
     *
     * @param trueRandom_ an object that implements the ITrueRandom interface
     * and supplies truly random bits for the session key. Note that it is
     * <strong>not</strong> sufficient to use a pseudo-random number 
     * generator (PRNG) seeded by the system clock; for an explanation,
     * see <a href="http://www.ietf.org/rfc/rfc1750.txt">rfc1750 
(Randomness Recommendations for Security)</a><p>
     *
     * On a Linux system (or any system that provides the /dev/random 
     * device), you can use the DevRandom class, which implements the 
     * ITrueRandom interface. On other operating systems you will have to 
     * provide your own source of true random bits. See 
     * <a href="http://www.cs.berkeley.edu/~daw/rnd/">Randomness for Crypto</a>
     * for information on generating sufficiently random numbers.
     */
    public void sendSessionKey(ITrueRandom trueRandom_)
	throws SSHProtocolException, SSHSetupException, IOException
    {
	if (_phase != PHASE_SEND_SESSION_KEY) {
	    throw new RuntimeException(
		"SSH protocol not in valid state for sending session key");
	}

	/* Send an SSH_CMSG_SESSION_KEY message in reply.
	 */
	CMSG_SESSION_KEY session_key_packet = 
		new CMSG_SESSION_KEY(trueRandom_, _server_public_key_packet);
	_session_id = session_key_packet.getSessionID();    // save it.
	_ssh_out.writePacket(session_key_packet);
	debug("Sent encrypted session key.");

	/* Now we have exchanged session keys, enable encryption.
	 */
	String cipherName = session_key_packet.getCipherName();
	Cipher sendCipher =
	    Cipher.getInstance(cipherName);
	if (sendCipher == null) {
	    throw new SSHSetupException(cipherName + 
		" algorithm not supported");
	}
        debug("Encryption type: " + cipherName);
	sendCipher.setKey(session_key_packet.getSessionKey());
	_ssh_out.setCipher(sendCipher);

	Cipher receiveCipher =
	    Cipher.getInstance(cipherName);
	receiveCipher.setKey(session_key_packet.getSessionKey());
	_ssh_in.setCipher(receiveCipher);

	Packet packet = readNextPacket();
	if (packet.getPacketType() != SSH_SMSG_SUCCESS) {
	    throw new SSHProtocolException("Received packet type " + 
		packet.getPacketType() + ", expected SSH_SMSG_SUCCESS");
	}
	debug("Received encrypted confirmation.");

	_phase = PHASE_DECLARE_USER;
    }

    /** Declares the username to the server. This method must be called
     * AFTER sendSessionKey() has returned successfully.
     * @return false if the server is willing to accept the user without
     * further authentication; true if authentication is required.
     */
    public boolean declareUser(String username_) 
	throws IOException, SSHProtocolException
    {
	if (_phase != PHASE_DECLARE_USER) {
	    throw new RuntimeException(
		"SSH protocol in invalid state for declaring user");
	}
	CMSG_USER user_packet = new CMSG_USER(username_);
	_ssh_out.writePacket(user_packet);

	Packet packet = readNextPacket();
	if (packet.getPacketType() == SSH_SMSG_SUCCESS) {
	    // The server has accepted our login; a password is not required.
	    _phase = PHASE_PREPARATORY;
	    return false;
	}

	if (packet.getPacketType() != SSH_SMSG_FAILURE) {
	    throw new SSHProtocolException("Received packet type " + 
		packet.getPacketType() + 
		", expected SSH_SMSG_SUCCESS or SSH_SMSG_FAILURE");
	}
	_phase = PHASE_AUTHENTICATE;
	return true;
    }

    /** Authenticate the user using username-password authentication.
     * This method must be called after declareUser() has returned 
     * successfully with a return value of <code>true</code>. 
     * @param user_ the username.
     * @param password_ the password string.
     * @return true if the server accepted the username and password.
     * @exception IOException if a network error occurred.
     * @exception SSHProtocolException if a malformed/invalid SSH packet
     * was received.
     */
    public boolean authenticateUser(String user_, String password_)
	throws IOException, SSHProtocolException
    {
	if (_phase != PHASE_AUTHENTICATE) {
	    throw new RuntimeException(
		"SSH protocol in invalid state for user authentication");
	}

	/* The server has sent us an encrypted packet and we have 
	 * succeeded in decrypting it. 
	 */
	debug("Doing password authentication.");

	/* The server sent us SSH_SMSG_FAILURE, which means that 
	 * authentication is required.
	 */
	CMSG_AUTH_PASSWORD password_packet = 
		new CMSG_AUTH_PASSWORD(password_);
	_ssh_out.writePacket(password_packet);

	Packet packet = readNextPacket();
	if (packet.getPacketType() != SSH_SMSG_SUCCESS) {
	    debug("User authentication failure");
	    return false;
	}

	_phase = PHASE_PREPARATORY;
	return true;
    }

    /** Authenticate the user using RSA authentication.
     * This method must be called after declareUser() has returned 
     * successfully with a return value of <code>true</code>.
     * @return true if the server authenticated us and authorized
     * us to log in.
     * @exception IOException if a network error occurs.
     * @exception SSHProtocolException is a malformed/invalid SSH packet
     * is received.
     */
    public boolean authenticateUser(RSAPrivateKey privateKey_)
	throws IOException, SSHProtocolException, SSHAuthFailedException
    {
	if (_phase != PHASE_AUTHENTICATE) {
	    throw new RuntimeException(
		"SSH protocol in invalid state for user authentication");
	}

	/* The server has sent us an encrypted packet and we have 
	 * succeeded in decrypting it. Now declare the user name.
	 */
	debug("Trying RSA authentication with key '" + 
	    privateKey_.getComment() + "'");

	/* The server sent us SSH_SMSG_FAILURE, which means that 
	 * authentication is required.
	 */
	byte[] modulus = privateKey_.getModulus();
	CMSG_AUTH_RSA auth_rsa = new CMSG_AUTH_RSA(modulus);
	_ssh_out.writePacket(auth_rsa);

	Packet packet = readNextPacket();
	int packet_type = packet.getPacketType();
	if (packet_type == SSH_SMSG_FAILURE) {
	    debug("Server refused our key");
	    return false;
	}
	else if (packet_type != SSH_SMSG_AUTH_RSA_CHALLENGE) {
	    throw new SSHProtocolException("Received packet type " +
		packet_type +
		", expected SSH_SMSG_AUTH_RSA_CHALLENGE or SSH_SMSG_FAILURE");
	}
	debug("Received RSA challenge from server.");

	SMSG_AUTH_RSA_CHALLENGE challenge_packet = (SMSG_AUTH_RSA_CHALLENGE) packet;
	byte[] challenge = challenge_packet.getChallenge();

	/* Now we must decrypt the challenge using our private key, 
	 * compute the MD5 checksum of the challenge plus the session id,
	 * and send back the resulting 16 bytes.
	 */
	byte[] serverRandomBytes = RSAAlgorithm.encrypt(
		challenge, 
		privateKey_.getExponent(), 
		privateKey_.getModulus());

	serverRandomBytes = RSAAlgorithm.stripPKCSPadding(serverRandomBytes);

	byte[] toBeHashed = 
	    SSHMisc.concatenate(serverRandomBytes, _session_id);

	byte[] response_bytes;
	try {
	    MessageDigest md5 = MessageDigest.getInstance("MD5");
	    response_bytes = md5.digest(toBeHashed);
	}
	catch (NoSuchAlgorithmException e) {
	    // Should never happen
	    throw new SSHProtocolException("MD5 algorithm not supported");
	}

	CMSG_AUTH_RSA_RESPONSE response_packet = 
		new CMSG_AUTH_RSA_RESPONSE(response_bytes);
	_ssh_out.writePacket(response_packet);

	packet = readNextPacket();
	packet_type = packet.getPacketType();
	if (packet_type == SSH_SMSG_FAILURE) {
	    debug("RSA authentication failed");
	    return false;
	}
	else if (packet_type != SSH_SMSG_SUCCESS) {
	    throw new SSHProtocolException("Received packet type " +
		packet_type +
		", expected SSH_MSG_SUCCESS or SSH_SMSG_FAILURE");
	}

	_phase = PHASE_PREPARATORY;
	return true;
    }

    /** Performs preparatory operations such as requesting compression 
     * and port-forwarding.  This method must be called after the 
     * user has been successfully authenticated. 
     */
    public void preparatoryOperations()
	throws IOException, SSHProtocolException, SSHSetupException
    {
	if (_phase != PHASE_PREPARATORY) {
	    throw new RuntimeException(
		"Protocol not in preparatory phase");
	}

	byte[] terminalModes = {
	    CMSG_REQUEST_PTY.ICRNL, 1,
	    CMSG_REQUEST_PTY.ISIG, 1,
	    CMSG_REQUEST_PTY.ICANON, 1,
	    CMSG_REQUEST_PTY.ECHO, 1,
	    CMSG_REQUEST_PTY.ECHOE, 1,
	    CMSG_REQUEST_PTY.ECHOK, 1,
	    CMSG_REQUEST_PTY.ECHONL, 1,
	    CMSG_REQUEST_PTY.OPOST, 1,
	    CMSG_REQUEST_PTY.ONLCR, 1,
	    CMSG_REQUEST_PTY.CS8, 1,
	    CMSG_REQUEST_PTY.TTY_OP_END };

	int columns = _options.getTerminalSize().width;
	int rows = _options.getTerminalSize().height;
	CMSG_REQUEST_PTY pty_packet = 
		new CMSG_REQUEST_PTY(_options.getTerminalType(), 
		rows, columns, terminalModes);

	debug("Requesting pty.");
	_ssh_out.writePacket(pty_packet);

	Packet packet = readNextPacket();
	if (packet.getPacketType() != SSH_SMSG_SUCCESS) {
	    throw new SSHProtocolException(
		"server refused to allocate pseudo-tty");
	}

	/* Request the local port-forwardings that were specified by the user.
	 */
	Iterator iter = _options.getLocalForwardings();
	while (iter.hasNext()) {
	    PortForwarding pf = (PortForwarding) iter.next();
	    Channel channel = null;
	    try {
		channel = new Channel(this, pf.getListenPort(),
		    pf.getHostname(), pf.getHostPort());
	    }
	    catch (java.net.BindException e) {
		throw new java.net.BindException(
		    "Cannot bind to port " + pf.getListenPort() + 
		    "; " + e.getMessage());
	    }

	    channel.setDaemon(true);
	    channel.start();
	}

	/* Request the remote port-forwardings that were specified by the user.
	 */
	iter = _options.getRemoteForwardings();
	while (iter.hasNext()) {
	    PortForwarding pf = (PortForwarding) iter.next();
	    debug("Requesting forwarding of remote port " + 
		pf.getListenPort() + " to " +
		pf.getHostname() + ":" + pf.getHostPort());

	    CMSG_PORT_FORWARD_REQUEST forward_request =
		new CMSG_PORT_FORWARD_REQUEST(
		    pf.getListenPort(), pf.getHostname(), pf.getHostPort());
	    _ssh_out.writePacket(forward_request);

	    if (readNextPacket().getPacketType() != 
		    SSH_SMSG_SUCCESS) {
		throw new SSHSetupException("Server refused to forward port " +
		    "(listen-port=" + pf.getListenPort() +
		    " hostname=" + pf.getHostname() +
		    " host-port=" + pf.getHostPort() + ")");
	    }
	}

	/* Enable compression if it was requested by the user.
	 */
	if (_options.compressionEnabled()) {
	    debug("Requesting compression.");
	    CMSG_REQUEST_COMPRESSION compression_request =
		new CMSG_REQUEST_COMPRESSION(6);
	    _ssh_out.writePacket(compression_request);

	    if (readNextPacket().getPacketType() == SSH_SMSG_SUCCESS) {
		_ssh_out.setCompression(6);
		_ssh_in.setCompression();
	    }
	}

	_phase = PHASE_READY;
	return;
    }

    /** Start an interactive session. This method blocks until the session
     * is terminated.  The method must be called after the preparatory 
     * phase has completed successfully.
     */
    public void execShell()
	throws IOException, SSHProtocolException
    {
	if (_phase != PHASE_READY) {
	    throw new RuntimeException(
		"Protocol not in correct state");
	}

	/* Request the server to start up an interactive shell for us.
	 */
	debug("Requesting shell.");
	CMSG_EXEC_SHELL shell_packet = new CMSG_EXEC_SHELL();
	_ssh_out.writePacket(shell_packet);

	/* Start up a separate thread to read (keystroke) packets off 
	 * the client queue and send them to the server.
	 */
	Thread sendThread = new Thread(this);
	sendThread.setDaemon(true);
	sendThread.start();

	/* If the keepalive timeout is nonzero, start a thread to send
	 * SSH_MSG_IGNORE packets when the session has been idle for the
	 * specified time.
	 */
	int timeout = _options.getKeepaliveTimeout();
	if (timeout != 0) {
	    _keepaliveThread = new KeepaliveThread(timeout);
	    _keepaliveThread.setDaemon(true);
	    _keepaliveThread.start();
	}

	/* Read packets from the network input stream, decrypt them and 
	 * process them.
	 */
	debug("Entering interactive session.");
	for (;;) {
	    Packet packet = null;
	    try {
		packet = readNextPacket();
	    }
	    catch (EOFException e) {
		break;	    // the server closed the connection.
	    }

	    if (packet instanceof IInteractivePacket == false) {
		throw new SSHProtocolException(
		    "packet type " + packet.getPacketType() +
		    " received in interactive mode");
	    }

	    /* All packets that implement the IInteractivePacket interface
	     * implement the processPacket() method.
	     */
	    ((IInteractivePacket) packet).processPacket(this);

	    if (packet.getPacketType() == SSH_SMSG_EXITSTATUS) {
		SMSG_EXITSTATUS status_packet = (SMSG_EXITSTATUS) packet;
		debug("Exit status " + status_packet.getExitStatus());
	    }
	}	// end for loop

	/* The server closed the network connection; terminate the thread.
	 */
	return;
    }

    /**
     * Starts executing the given command, and enters interactive 
     * session mode. On UNIX, the command is run as 
     * "&lt;shell&gt; -c &lt;command&gt;" where &lt;shell&gt; is the user's
     * login shell.<p>
     *
     * This method is an alternative to <code>execShell()</code>. 
     * It must be called after the preparatory operations have
     * completed successfully.
     */
    public void execCmd(String command_)
	throws IOException, SSHProtocolException
    {
	if (_phase != PHASE_READY) {
	    throw new RuntimeException(
		"Protocol not in correct state");
	}

	/* Request the server to start up an interactive shell for us.
	 */
	debug("Sending command: " + command_);
	CMSG_EXEC_CMD cmd_packet = new CMSG_EXEC_CMD(command_);
	_ssh_out.writePacket(cmd_packet);

	/* Read packets from the network input stream, decrypt them and 
	 * process them.
	 */
	debug("Entering interactive session.");
	for (;;) {
	    Packet packet = null;
	    try {
		packet = readNextPacket();
	    }
	    catch (EOFException e) {
		break;	    // the server closed the connection.
	    }
	    catch (SocketException e) {
		break;	    // the server closed the connection.
	    }

	    if (packet instanceof IInteractivePacket == false) {
		throw new SSHProtocolException(
		    "packet type " + packet.getPacketType() +
		    " received in interactive mode");
	    }

	    ((IInteractivePacket) packet).processPacket(this);

	    if (packet.getPacketType() == SSH_SMSG_EXITSTATUS) {
		SMSG_EXITSTATUS status_packet = (SMSG_EXITSTATUS) packet;
		debug("Exit status " + status_packet.getExitStatus());
		_ssh_out.writePacket(new CMSG_EXIT_CONFIRMATION());
	    }
	}
    }

    /** This method runs in a separate thread, and handles the sending
     * of SSH protocol packets to the SSH server. It is started automatically
     * when the protocol handshake has been completed.
     */
    public void run() {

	for (;;) {
	    Packet packet = _clientqueue.getNextPacket();

	    try {
		_ssh_out.writePacket(packet);
	    }
	    catch (IOException e) {
		e.printStackTrace();
		System.exit(0);
	    }
	}
    }

    /** Registers an open SSH channel (encrypted tunnel).
     */
    public void registerOpenChannel(OpenChannel channel_) {
	_channelManager.registerOpenChannel(channel_);
    }

    /** Deregisters an open SSH chanel.
     */
    public void removeOpenChannel(OpenChannel channel_) {
	_channelManager.removeOpenChannel(channel_);
    }

    /** Finds the open channel with the specified channel number.
     */
    public OpenChannel findOpenChannel(int channel_number_) {
	return _channelManager.findOpenChannel(channel_number_);
    }

    /** This method is called when a SSH_MSG_PORT_OPEN message
     * is received; it returns true if the port open request matches
     * one of the local port forwardings specified by the user.
     */
    public boolean isPortOpenAllowed(String hostname_, int port_) {

	// Delegate to the Options member variable.
	return _options.isPortOpenAllowed(hostname_, port_);
    }

    /** Returns the SSH_SMSG_PUBLIC_KEY packet that was sent by
     * the server.
     */
    public SMSG_PUBLIC_KEY getServerKeyPacket() {
	return _server_public_key_packet;
    }

    /** Private helper method to read the next SSH packet.
     * DEBUG packets are logged but are not returned to the caller.
     */
    private Packet readNextPacket() 
	throws IOException, SSHProtocolException
    {
	Packet packet = null;
	while (true) {
	    packet = _ssh_in.readPacket();

	    // Reception of any SSH packet should reset the keepalive timer.
	    if (_keepaliveThread != null) {
		_keepaliveThread.interrupt();
	    }

	    int packet_type = packet.getPacketType();
	    if (packet_type == SSH_MSG_DEBUG) {
		debug(((MSG_DEBUG) packet).getMessage());
	    }
	    else if (packet_type != SSH_MSG_IGNORE)
		break;
	}
	return packet;
    }

    /** Display the specified debugging message.
     */
    public void debug(String string_) {
	if (_options.getDebug())
	    enqueueToStdout(new SMSG_STDERR_DATA("debug: " + string_ + "\r\n"));
    }

    /** A nonstatic inner class which sends SSH_MSG_IGNORE packets to keep the
     * session alive.
     */
    private class KeepaliveThread extends Thread
    {
	private int _seconds;

	public KeepaliveThread(int seconds_) {
	    _seconds = seconds_;
	}

	public void run() {
	    for (;;) {
		try {
		    Thread.sleep(_seconds * 1000L);
		}
		catch (InterruptedException e) {
		    /* The sleep was interrupted by transmission or reception
		     * of a packet. Start a new sleep.
		     */
		    continue;
		}
		/* If we get here, it means that we completed our sleep without
		 * being interrupted by reception of an SSH packet. So send a 
		 * SSH_MSG_IGNORE packet to keep the session alive.
		 */
		_clientqueue.enqueue(new MSG_IGNORE());
	    }	    // end for loop
	}
    }

    //====================================================================
    // INSTANCE VARIABLES

    /** This queue holds packets to be sent by this client.
     */
    private PacketQueue _clientqueue = new PacketQueue();

    /** This is the network socket connected to the remote server.
     */
    private Socket _socket;

    /** Encapsulates all the command-line options.
     */
    private Options _options;

    /** This is the input stream associated with the SSH network connection.
     */
    private BufferedInputStream _instream;

    /** This is the output stream associated with the SSH network connection.
     */
    private BufferedOutputStream _outstream;

    /** The public key packet received from the server is stored here
     * for later use.
     */
    private SMSG_PUBLIC_KEY _server_public_key_packet;

    /** The session id (extracted from the SSH_CMSG_SESSION_KEY packet)
     * is stored here for later use.
     */
    private byte[] _session_id;

    private SSHInputStream _ssh_in;

    private SSHOutputStream _ssh_out;

    private STDOUT_InputStream _stdout = new STDOUT_InputStream();

    private STDIN_OutputStream _stdin = null;

    private StringBuffer _id_string_received = new StringBuffer();

    private static String ID_STRING_SENT = "SSH-1.5-JSSH 0.9.5 (2002/10/05)";

    private ChannelManager _channelManager = new ChannelManager();

    private KeepaliveThread _keepaliveThread = null;

    private int _phase = PHASE_EXCHANGE_ID_STRINGS;

    // The protocol must proceed through each of these phases in turn.
    private static final int PHASE_EXCHANGE_ID_STRINGS = 1;
    private static final int PHASE_RECEIVE_SERVER_KEY = 2;
    private static final int PHASE_SEND_SESSION_KEY = 3;
    private static final int PHASE_DECLARE_USER = 4;
    private static final int PHASE_AUTHENTICATE = 5;
    private static final int PHASE_PREPARATORY = 6;
    private static final int PHASE_READY = 7;

}
