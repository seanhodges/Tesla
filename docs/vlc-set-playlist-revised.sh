#!/bin/bash

selectedTrackUri="file:///home/sean/Music/Coldplay/Parachutes/Coldplay - 05 - Yellow.mp3"; 
listLength=$(dbus-send --session --print-reply --dest=org.mpris.vlc --type="method_call" /TrackList org.freedesktop.MediaPlayer.GetLength 2>/dev/null | grep int32 | sed -e 's/   //' | cut -d ' ' -f 2); 
trackUri=""; 
for (( i=0; i<$listLength; i++ )); 
do 
	oldTrackUri=$trackUri; 
	until [[ $trackUri != $oldTrackUri ]]; 
	do 
		trackUri=$(dbus-send --session --print-reply --dest=org.mpris.vlc --type="method_call" /TrackList org.freedesktop.MediaPlayer.GetMetadata int32:0 2>/dev/null | sed -e "s/'/\\'/g");
		trackUri=${trackUri#*location*string*\"}; 
		trackUri=${trackUri%%\"*)*]}; 
	done; 
	dbus-send --session --print-reply --dest=org.mpris.vlc --type="method_call" /TrackList org.freedesktop.MediaPlayer.DelTrack int32:0 &>/dev/null; 
	if [[ "$selectedTrackUri" != "$trackUri" ]]; then 
		dbus-send --session --print-reply --dest=org.mpris.vlc --type="method_call" /TrackList org.freedesktop.MediaPlayer.AddTrack string:"$trackUri" boolean:false &>/dev/null;
	else 
		dbus-send --session --print-reply --dest=org.mpris.vlc --type="method_call" /TrackList org.freedesktop.MediaPlayer.AddTrack string:"$trackUri" boolean:true &>/dev/null; 
	fi; 
done
