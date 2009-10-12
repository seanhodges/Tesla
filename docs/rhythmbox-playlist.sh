#!/bin/bash

echo [rhythmdb];
rhythmdb_path=~/.gnome2/rhythmbox/rhythmdb.xml; 
if test -e ~/.local/share/rhythmbox/rhythmdb.xml; then 
	rhythmdb_path=~/.local/share/rhythmbox/rhythmdb.xml; 
fi; 
python -c "
import libxml2; 
doc = libxml2.parseFile('${rhythmdb_path}'); 
ctxt = doc.xpathNewContext(); 
res = ctxt.xpathEval('//entry[@type=\"song\"]/album[.=\"Eyes Open\"]/../artist[.=\"Snow Patrol\"]/..'); 
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

