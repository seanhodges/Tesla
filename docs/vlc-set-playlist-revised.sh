#!/bin/bash

selectedTrackUri="file:///home/sean/Music/Coldplay/Parachutes/Coldplay - 05 - Yellow.mp3"
listLength=$(dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.GetLength | grep int32 | sed -e "s/   //" | cut -d ' ' -f 2)
trackUri=""
for (( i=0; i<$listLength; i++ ))
do
	# Get the title
	oldTrackUri=$trackUri
	until [[ $trackUri != $oldTrackUri ]]
	do
		trackUri=$(dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.GetMetadata int32:0 2>/dev/null | sed -e "s/'/\\'/g")
		trackUri=${trackUri#*location*string*\"}
		trackUri=${trackUri%%\"*)*]}
	done
	# Remove track from top of list
	dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.DelTrack int32:0
	# Add it at the end
	if [[ "$selectedTrackUri" != "$trackUri" ]]; then
		dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.AddTrack string:"$trackUri" boolean:false
	else
		# If it matches the selected item, append the "play now" attribute
		dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.AddTrack string:"$trackUri" boolean:true
	fi
done
