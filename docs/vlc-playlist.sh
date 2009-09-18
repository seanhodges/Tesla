#!/bin/bash
listLength=$(dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.GetLength | grep int32 | sed -e "s/   //" | cut -d ' ' -f 2)
for (( i=1; i<=$listLength; i++ ))
do
	dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.GetMetadata int32:$i
done

#dbus-send --print-reply --dest=org.mpris.vlc /TrackList org.freedesktop.MediaPlayer.GetMetadata int32:12
