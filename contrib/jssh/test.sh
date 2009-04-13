# This script runs the test program.

# Turn off echoing of input characters, and disable the erase
# and other control characters.
stty -icanon -echo

# Run the SSH client, connecting to "localhost". The command-line 
# options are (mostly) the same as for the standard OpenSSH client.
# This example sets up a local port-forwarding from port 8303 to
# port 8005 on the server. It also sets the "keepalive timeout" to
# 60 seconds.

$JAVA_HOME/bin/java ${TEST_OPTS} \
-cp classes \
jssh.SSHClient \
-v \
-C \
-l $LOGNAME \
-L 8303:localhost:8005 \
-p 22 \
-kt 60 \
localhost 

# Return the terminal to a sane state.
stty sane
