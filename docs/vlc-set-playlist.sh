#!/bin/bash
selection=4
playing=$(dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.GetCurrentTrack | grep int32 | sed -e "s/   //" | cut -d ' ' -f 2)
until [[ $playing == $selection ]]
do
	echo $playing
	if [[ $playing < $selection ]]; then
		dbus-send --print-reply --dest=org.mpris.vlc /Player org.freedesktop.MediaPlayer.Next
	elif [[ $playing > $selection ]]; then
		dbus-send --print-reply --dest=org.mpris.vlc /Player org.freedesktop.MediaPlayer.Prev
	fi
	sleep 1
	let playing=$(dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.GetCurrentTrack | grep int32 | sed -e "s/   //" | cut -d ' ' -f 2)
done
