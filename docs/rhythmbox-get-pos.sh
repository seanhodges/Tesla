echo [dbus];dbus-send --session --print-reply --dest=org.gnome.Rhythmbox --type="method_call" /org/gnome/Rhythmbox/Player org.gnome.Rhythmbox.Player.getElapsed 2>/dev/null; 
if [[ $? != "0" ]]; then echo -e 'method return sender=:1.74 -> dest=:1.83 reply_serial=2\n   uint32 0'; fi
