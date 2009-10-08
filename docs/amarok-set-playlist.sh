#selectedTrackUri="file:///media/data/Music/Newton%20Faulkner/Hand%20Built%20By%20Robots/14%20%20Newton%20Faulkner%20-%20UFO.mp3"; listLength=$(dbus-send --session --print-reply --dest=org.kde.amarok --type="method_call" /TrackList org.freedesktop.MediaPlayer.GetLength 2>/dev/null | grep int32 | sed -e 's/   //' | cut -d ' ' -f 2); trackUri=""; for (( i=0; i<$listLength; i++ )); do oldTrackUri=$trackUri; until [[ "$trackUri" != "$oldTrackUri" ]]; do trackUri=$(dbus-send --session --print-reply --dest=org.kde.amarok --type="method_call" /TrackList org.freedesktop.MediaPlayer.GetMetadata int32:0 2>/dev/null | sed -e "s/'/\'/g"); trackUri=${trackUri#*location*string*\"}; trackUri=${trackUri%%\"*)*]}; done; dbus-send --session --print-reply --dest=org.kde.amarok --type="method_call" /TrackList org.freedesktop.MediaPlayer.DelTrack int32:0; if [[ "$selectedTrackUri" != "$trackUri" ]]; then dbus-send --session --print-reply --dest=org.kde.amarok --type="method_call" /TrackList org.freedesktop.MediaPlayer.AddTrack string:"$trackUri" boolean:false &>/dev/null; else dbus-send --session --print-reply --dest=org.kde.amarok --type="method_call" /TrackList org.freedesktop.MediaPlayer.AddTrack string:"$trackUri" boolean:true &>/dev/null; fi; done
if [[ "$(dcop --all-users amarok player version 2>/dev/null)" != "" ]]; then 
	echo [dcop];
	dcop --all-users amarok playlist playByIndex 2; 
else 
	selectedTrackUri="%s"; 
	listLength=$(dbus-send --session --print-reply --dest=org.kde.amarok --type="method_call" /TrackList org.freedesktop.MediaPlayer.GetLength 2>/dev/null | grep int32 | sed -e 's/   //' | cut -d ' ' -f 2); trackUri=""; 
	for (( i=0; i<$listLength; i++ )); do 
		oldTrackUri=$trackUri; 
		until [[ "$trackUri" != "$oldTrackUri" ]]; do 
			trackUri=$(dbus-send --session --print-reply --dest=org.kde.amarok --type="method_call" /TrackList org.freedesktop.MediaPlayer.GetMetadata int32:0 2>/dev/null | sed -e "s/'/\\'/g"); 
			trackUri=${trackUri#*location*string*\"}; 
			trackUri=${trackUri%%\"*)*]}; 
		done; 
		dbus-send --session --print-reply --dest=org.kde.amarok --type="method_call" /TrackList org.freedesktop.MediaPlayer.DelTrack int32:0; 
		if [[ "$selectedTrackUri" != "$trackUri" ]]; then 
			dbus-send --session --print-reply --dest=org.kde.amarok --type="method_call" /TrackList org.freedesktop.MediaPlayer.AddTrack string:"$trackUri" boolean:false &>/dev/null; 
		else 
			dbus-send --session --print-reply --dest=org.kde.amarok --type="method_call" /TrackList org.freedesktop.MediaPlayer.AddTrack string:"$trackUri" boolean:true &>/dev/null; 
		fi; 
	done; 
fi
