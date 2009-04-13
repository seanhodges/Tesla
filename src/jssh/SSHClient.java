/* class SSHClient
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

import java.net.*;
import java.io.*;
import java.util.*;
import java.math.BigInteger;
import de.mud.ssh.Cipher;

/**
 * This is an example of a program that uses the JSSH library to connect
 * to a remote host using the SSH protocol (version 1.5).<p>
 *
 * This program should be run from the script "test.sh", which disables
 * echoing on the input stream, and also disables processing of control
 * characters on input.
 *
 * This particular example uses the stdin and stdout streams, and starts
 * up two threads (implemented by the nonstatic inner classes ReaderThread
 * and WriterThread) to read and write these streams. Instead of using the
 * stdin and stdout streams, one could start up a terminal-emulator
 * (assuming that the emulator provides an input stream and an output stream)
 * and read and write the terminal-emulator's streams.
 */
public class SSHClient
{
    /** Constructor
     */
    public SSHClient(Options options_)
    {
	_options = options_;
    }

    /** This method connects to the server and starts up the protocol
     * handler.
     */
    public void connect() 
	throws Exception
    {

	/* Resolve the server address.
	 */
	InetAddress serverAddress = null;
	String servername = _options.getHostname();
	try {
	    serverAddress = InetAddress.getByName(servername);
	}
	catch (UnknownHostException e) {
	    System.err.println("  Unknown host " + servername);
	    System.exit(-1);
	}

	/* Start up a separate thread to terminate the authentication
	 * process if it doesn't complete within 60 seconds.
	 */
	TimeoutThread timeout_thread = new TimeoutThread();
	timeout_thread.setDaemon(true);
	timeout_thread.start();

	System.out.print("  Connecting to " + servername + "...");
	Socket socket = new Socket(serverAddress, _options.getPort());

	System.out.println("  Connected");

	/* Create an SSH protocol handler.
	 */
	_handler = new ClientProtocolHandler(socket, _options);

	/* Start a thread to read debugging information from the 
	 * STDOUT_InputStream
	 */
	ReaderThread reader_thread = 
		new ReaderThread(_handler.getSTDOUT());
	// The application must exit if this is the only thread left.
	reader_thread.setDaemon(true);
	    reader_thread.start();

	/* Now we are connected to the server, exchange version
	 * information as required by the SSH protocol.
	 */
	_handler.exchangeIdStrings();

	_handler.receiveServerKey();

	/* Check that the server's host key is in the known_hosts file
	 * (but skip the check if we are connecting to localhost).
	 */
	String server = socket.getInetAddress().getHostAddress();
	if (server.equals("127.0.0.1") == false) {
	    check_host_key(_handler.getServerKeyPacket(), servername);
	}
	else
	    debug("Forcing acceptance of host key for localhost");

	/* Instantiate an object that reads true random bits from the
	 * /dev/urandom device. This works on Linux and any other OS
	 * that has /dev/random or /dev/urandom; on other OS's, such 
	 * as Windoze, you'll have to supply your own object that 
	 * implements the TrueRandom interface.
	 * Note that it is preferable to use /dev/random, but /dev/random
	 * blocks if it doesn't have the requested number of bits in its
	 * entropy pool, so it can cause unacceptably long delays.
	 */
	ITrueRandom trueRandom = new DevURandom();
	_handler.sendSessionKey(trueRandom);

	/* Get the username - either from the command-line, or
	 * interactively if the command-line option is not set.
	 */
	InputStreamReader inreader = new InputStreamReader(System.in);
	BufferedReader keyboardReader = new BufferedReader(inreader);

	String userName = _options.getUser();

	boolean authRequired = _handler.declareUser(userName);
	if (authRequired) {

	    RSAPrivateKeyFile keyfile = null;
	    try {
		keyfile = new RSAPrivateKeyFile( _options.getIdentityFile());
	    }
	    catch (FileNotFoundException e) {}

	    // If the private-key file exists, try RSA authentication
	    // first.
	    if (keyfile != null) {
		RSAPrivateKey privateKey = null;
		if (keyfile.getCipherType() != Cipher.SSH_CIPHER_NONE) {
		    System.out.print("Enter passphrase for RSA key '" +
			_options.getIdentityFile() + "': ");
		    String passphrase = keyboardReader.readLine();

		    // We have to output a newline here because the terminal
		    // is in non-echo mode.
		    System.out.println();
		    privateKey = keyfile.getPrivateKey(passphrase);
		}
		else {
		    privateKey = keyfile.getPrivateKey();
		}

		if (_handler.authenticateUser(privateKey) == true) {
		    authRequired = false;
		}
	    }
	    else {
		_handler.debug("unknown identity file " + 
			_options.getIdentityFile());
	    }

	    // Either the private-key file doesn't exist, or RSA
	    // authentication failed.
	    if (authRequired) {
		System.out.print("Password for " + userName + ": ");
		String password = keyboardReader.readLine();

		// We have to output a newline here because the terminal
		// is in non-echo mode.
		System.out.println();
		if ( ! _handler.authenticateUser(userName, password))
		    throw new SSHAuthFailedException();
	    }
	}

	/* Request compression, port-forwarding etc.
	 */
	_handler.preparatoryOperations();

	/* The setup exchange is complete; we can now start the 
	 * emulator.
	 */
	_setupSucceeded = true;

	String command = _options.getCommand();
	if (command != null) {
	    _handler.execCmd(command);
	}
	else {
	    // Start up a thread to read from stdin and write to
	    // STDIN_OutputStream.
	    STDIN_OutputStream out = _handler.getSTDIN();
	    WriterThread writer_thread = new WriterThread(out);
	    writer_thread.start();

	    // Blocks until session termination.
	    _handler.execShell();

	    /* The emulator returned because the remote host closed the 
	     * connection, or the user typed "<ENTER> ~ .")
	     */
	}
	return;
    }

    /** Main method of the example program.
     */
    public static void main(String[] argv)
    {
	Options options = new Options();
	getOptions(argv, options);

	if (options.getUser() == null)
	    usage();	// username must be specified on command-line

	if (options.getHostname() == null)
	    usage();	// hostname must be specified on command-line

	options.setTerminalType("xterm");   // hard-coded for this example..

	SSHClient client = new SSHClient(options);
	try {
	    client.connect();
	}
	catch (Exception e) {

	    if (e instanceof java.net.ConnectException) {
		System.err.println("Cannot connect to " + 
		    options.getHostname() + ": " + e.getMessage());
	    }
	    else if (e instanceof IOException)
		System.err.println("IOException: " + e.getMessage());
	    else if (e instanceof SSHSetupException)
		System.err.println("SSHSetupException: " + e.getMessage());
	    else if (e instanceof SSHProtocolException)
		System.err.println("SSHProtocolException: " + e.getMessage());
	    else if (e instanceof SSHAuthFailedException)
		System.err.println("Authentication failed");
	    else
		e.printStackTrace();
	}
	System.exit(0);
    }

    /** Parse the command-line options.
     */
    private static void getOptions(String[] argv_, Options options_) {
	for (int i=0; i<argv_.length; i++) {
	    if (argv_[i].startsWith("-") == false) {
		if (i == argv_.length - 1) {
		    // This is the last argument.
		    options_.setHostname(argv_[i]);
		}
		else if (i == argv_.length - 2) {
		    // This is the second-last argument.
		    options_.setHostname(argv_[i++]);
		    options_.setCommand(argv_[i]);
		}
		else
		    usage();

	    }

	    else if (argv_[i].equals("-v"))
		options_.setDebug(true);

	    else if (argv_[i].equals("-C"))
		options_.setCompression(true);

	    else if (argv_[i].equals("-l")) {
		if (i == argv_.length - 1)
		    usage();

		options_.setUser(argv_[++i]);
	    }

	    else if (argv_[i].equals("-i")) {
		if (i == argv_.length - 1)
		    usage();

		options_.setIdentityFile(argv_[++i]);
	    }

	    else if (argv_[i].equals("-p")) {
		if (i == argv_.length - 1)
		    usage();

		int port = 0;
		try {
		    port = Integer.parseInt(argv_[++i]);
		}
		catch (Exception e) {
		    usage();
		}
		options_.setPort(port);
	    }

	    else if (argv_[i].equals("-L")) {
		if (i == argv_.length - 1)
		    usage();

		StringTokenizer st = new StringTokenizer(argv_[++i], ":");
		String host = null;
		int listenport = 0;
		int hostport = 0;
		try {
		    String listenport_str = st.nextToken();
		    listenport = Integer.parseInt(listenport_str);
		    host = st.nextToken();
		    String hostport_str = st.nextToken();
		    hostport = Integer.parseInt(hostport_str);
		}
		catch (Exception e) {
		    usage();
		}
		options_.addLocalPortForwarding(
		    new PortForwarding(listenport, host, hostport));
	    }

	    else if (argv_[i].equals("-R")) {
		if (i == argv_.length - 1)
		    usage();

		StringTokenizer st = new StringTokenizer(argv_[++i], ":");
		String host = null;
		int listenport = 0;
		int hostport = 0;
		try {
		    String listenport_str = st.nextToken();
		    listenport = Integer.parseInt(listenport_str);
		    host = st.nextToken();
		    String hostport_str = st.nextToken();
		    hostport = Integer.parseInt(hostport_str);
		}
		catch (Exception e) {
		    usage();
		}

		options_.addRemotePortForwarding(
		    new PortForwarding(listenport, host, hostport));
	    }

	    else if (argv_[i].equals("-a")) {
		if (i == argv_.length - 1)
		    usage();

		/* Get, process and save the answerback string.
		 */
		String answerback = argv_[++i];

		/* Parse the answerback string and look for escape chars such
		 * as "\r" and "\t". The standard C/Java escape characters 
		 * are handled.
		 */
		StringBuffer buf = new StringBuffer();
		for (int j=0; j<answerback.length(); j++) {
		    if (answerback.charAt(j) != '\\')
			buf.append(answerback.charAt(j));
		    else if (++j < answerback.length()) {
			switch (answerback.charAt(j)) {
			    case '\\':
				buf.append('\\'); break;
			    case 'r':
				buf.append('\r'); break;
			    case 'n':
				buf.append('\n'); break;
			    case 't':
				buf.append('\t');
			}
		    }
		}
		System.setProperty("term.answerback", buf.toString());
	    }

	    else if (argv_[i].equals("-kt")) {
		if (i == argv_.length - 1)
		    usage();

		int timeout = 0;
		try {
		    timeout = Integer.parseInt(argv_[++i]);
		}
		catch (Exception e) {
		    usage();
		}
		options_.setKeepaliveTimeout(timeout);
		
	    }

	    else
		usage();
	}
    }

    /** Display a usage message and die.
     */
    private static void usage() {
	System.err.println("Usage: java sshclient.SSHClient [options] host [command]");
	System.err.println("Options:");
	System.err.println("  -l user      Log in using this user name");
	System.err.println("  -C           Enable compression");
	System.err.println("  -v           Display verbose debugging messages");
	System.err.println("  -p port      Connect to this port on the server");
	System.err.println("  -i file      Identity for public-key " +
	    "authentication (default: $HOME/.ssh/identity)");
	System.err.println("  -L listen-port:host:host-port   Forward local " +
	    "port to remote address");
	System.err.println("  -R listen-port:host:host-port   Forward remote " +
	    "port to local address");
	System.err.println("  -a answerback-string");
	System.err.println("  -kt keepalive-timeout");
	System.exit(-1);
    }

    /** Check if the received server key is in the known_hosts file.
     */
    private void check_host_key(SMSG_PUBLIC_KEY packet_, String servername_) 
	throws IOException, SSHSetupException
    {
	BigInteger host_key_public_modulus =
	    new BigInteger(1, packet_.getHostKeyPublicModulus());
	BigInteger host_key_public_exponent =
	    new BigInteger(1, packet_.getHostKeyPublicExponent());

	// First check in the global known_hosts file.
	KnownHostsFile globalKnownHosts = 
	    new KnownHostsFile("/etc/ssh/ssh_known_hosts");

	int status = globalKnownHosts.check_host_key(
	    servername_,
	    host_key_public_modulus,
	    host_key_public_exponent);

	String user_known_hosts = 
	    System.getProperty("user.home") + "/.ssh/known_hosts";
	KnownHostsFile userKnownHosts =
	    new KnownHostsFile(user_known_hosts);

	if (status == KnownHostsFile.HOST_KEY_NEW) {
	    // It wasn't there, so check the user known_hosts.
	    status = userKnownHosts.check_host_key(
		servername_,
		host_key_public_modulus,
		host_key_public_exponent);
	}

	/* If the server's host key was not found in either of
	 * the known_hosts files, add it to the user's known_hosts.
	 */
	if (status == KnownHostsFile.HOST_KEY_NEW) {
	    debug("Adding host key to " + user_known_hosts);
	    userKnownHosts.add_host_key(
		servername_,
		host_key_public_modulus,
		host_key_public_exponent);
	}
	else if (status == KnownHostsFile.HOST_KEY_DIFFERS) {
	    throw new SSHSetupException("Server host key changed!");
	}
    }

    private void debug(String string_) {
	if (_options.getDebug())
	    System.err.println("debug: " + string_);
    }

    //====================================================================
    // INSTANCE VARIABLES

    private Options _options;

    private boolean _setupSucceeded = false;

    private ClientProtocolHandler _handler;

    /** A nonstatic inner class that is used to time-out the authentication
     * process after 60 seconds.
     */
    private class TimeoutThread
	extends Thread
    {
	TimeoutThread() {}

	/**
	 * Check for a timeout in the authentication.
	 */
	public void run()
	{
	    try {
		Thread.sleep(60000);    // 60 seconds
	    }
	    catch (InterruptedException e) {}

	    if (SSHClient.this._setupSucceeded == false) {
		System.err.println("  timeout in authentication.");
		System.exit(1);
	    }
	}
    }	    // end of inner class.

    /** A nonstatic inner class that reads from the STDOUT_InputStream and
     * copies to the stdout stream.
     */
    private class ReaderThread
	extends Thread
    {
	private InputStream _instream;
	private byte[] buf = new byte[132];

	ReaderThread(STDOUT_InputStream instream_) {
	    _instream = instream_;
	}

	public void run() {
	    for (;;) {
		int nbytes;
		try {
		    nbytes = _instream.read(buf, 0, buf.length);
		    if (nbytes > 0)
			System.out.write(buf, 0, nbytes);
		    else {
			return;
		    }
		}
		catch (IOException e) {
		    e.printStackTrace();
		    return;
		}

	    }
	}
    }

    /** A nonstatic inner class that reads from the keyboard and
     * copies to the STDIN_OutputStream
     */
    private class WriterThread
	extends Thread
    {
	private OutputStream _outstream;
	private byte[] buf = new byte[132];

	WriterThread(STDIN_OutputStream outstream_) {
	    _outstream = outstream_;
	}

	public void run() {
	    for (;;) {
		int nbytes;
		try {
		    nbytes = System.in.read(buf, 0, buf.length);
		    if (nbytes > 0)
			_outstream.write(buf, 0, nbytes);
		    else {
			_outstream.close();
			return;
		    }
		}
		catch (IOException e) {
		    e.printStackTrace();
		    return;
		}
	    }
	}
    }

    private static final int CTRL_C = 3;
}
