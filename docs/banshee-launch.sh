dbus-send --print-reply --dest=org.bansheeproject.Banshee /org/bansheeproject/Banshee/ClientWindow org.bansheeproject.Banshee.ClientWindow.Present &>/dev/null; 
if [[ $? != 0 ]]; then 
	DISPLAY=:0 banshee-1 &>/dev/null & sleep 5; 
fi; 
echo success
