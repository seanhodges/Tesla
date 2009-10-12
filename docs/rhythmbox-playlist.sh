#!/bin/bash

uri="file:///home/sean/Music/Avril%20Lavigne/Let%20Go/03%20-%20Sk8er%20Boi.mp3"

echo [rhythmdb];
rhythmdb_path=~/.gnome2/rhythmbox/rhythmdb.xml; 
if test -e ~/.local/share/rhythmbox/rhythmdb.xml; then 
	rhythmdb_path=~/.local/share/rhythmbox/rhythmdb.xml; 
fi; 
python -c "
import libxml2; 
doc = libxml2.parseFile('${rhythmdb_path}'); 
ctxt = doc.xpathNewContext(); 
res = ctxt.xpathEval('//entry[@type=\"song\"]/location[.=\"${uri}\"]/..'); 
print res;
for item in res: 
	print item;
	artist = res.get('artist');
	album = res.get('album');
res = ctxt.xpathEval('//entry[@type=\"song\"]/artist[.=\"' + artist + '\"]/album[.=\"' + album + '\"]/..'); 
print \"<playlist>\";
for item in res: 
	print item; 
print \"</playlist>\";
ctxt.xpathFreeContext(); 
doc.freeDoc()" 

exit

rhythmdb_path=~/.gnome2/rhythmbox/rhythmdb.xml; if test -e ~/.local/share/rhythmbox/rhythmdb.xml; then rhythmdb_path=~/.local/share/rhythmbox/rhythmdb.xml; fi; uri="$(qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player getPlayingUri | sed -e "s/'/\\\'/g")"; 
if [[ ${uri} != "" ]]; then
python -c "
import libxml2; 
doc = libxml2.parseFile('${rhythmdb_path}'); 
ctxt = doc.xpathNewContext(); 
res = ctxt.xpathEval('//entry[@type=\"song\"]/location[.=\"${uri}\"]/../track-number'); 
print res[0]; 
ctxt.xpathFreeContext(); doc.freeDoc()"

# Get artist and album
artist="Snow Patrol"
album="Eyes Open"

# Query for matching album tracks
python -c "
import libxml2; 
doc = libxml2.parseFile('${rhythmdb_path}'); 
ctxt = doc.xpathNewContext(); 
res = ctxt.xpathEval('//entry[@type=\"song\"]/album[.=\"${album}\"]/../artist[.=\"${artist}\"]/..'); 
for item in res:
	print item; 
ctxt.xpathFreeContext(); doc.freeDoc()"

fi

