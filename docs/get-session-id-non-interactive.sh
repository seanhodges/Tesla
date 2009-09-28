#!/bin/bash

XDG_ID="$(echo $XDG_SESSION_COOKIE | cut -d '-' -f 1)"; 

echo This should be interactive
bash -i -c "XDG_ID=\"$(echo $XDG_SESSION_COOKIE | cut -d '-' -f 1)\"; grep DBUS_SESSION_BUS_ADDRESS= ~/.dbus/session-bus/${XDG_ID}-0 | cut -d '=' -f 2,3,4"

echo This should be non-interactive. It will still have an attached TTY though
bash --noprofile --norc -c "XDG_ID=\"$(echo $XDG_SESSION_COOKIE | cut -d '-' -f 1)\"; grep DBUS_SESSION_BUS_ADDRESS= ~/.dbus/session-bus/${XDG_ID}-0 | cut -d '=' -f 2,3,4"

echo This SSH\'s onto the machine, and retrieves the session ID remotely
ssh sean@localhost "XDG_ID=\"$(echo $XDG_SESSION_COOKIE | cut -d '-' -f 1)\"; grep DBUS_SESSION_BUS_ADDRESS= ~/.dbus/session-bus/${XDG_ID}-0 | cut -d '=' -f 2,3,4"

