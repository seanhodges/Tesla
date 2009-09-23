#!/bin/bash

rm /tmp/iout.txt
rm /tmp/sout.txt

# This should be interactive
bash -i -c "grep DBUS_SESSION_BUS_ADDRESS= ~/.dbus/session-bus/*-0 | cut -d '=' -f 2,3,4 &> /tmp/iout.txt"

# This should be non-interactive. It will still have an attached TTY though
bash --noprofile --norc -c "grep DBUS_SESSION_BUS_ADDRESS= ~/.dbus/session-bus/*-0 | cut -d '=' -f 2,3,4 &> /tmp/sout.txt"

