#!/bin/bash

rhythmdb_path=~/.gnome2/rhythmbox/rhythmdb.xml; if test -e ~/.local/share/rhythmbox/rhythmdb.xml; then rhythmdb_path=~/.local/share/rhythmbox/rhythmdb.xml; fi; uri="$(qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player getPlayingUri | sed -e "s/'/\\\'/g")"; 
if [[ ${uri} != "" ]]; then
python -c "
import libxml2; 
doc = libxml2.parseFile('${rhythmdb_path}'); 
ctxt = doc.xpathNewContext(); 
res = ctxt.xpathEval('//entry[@type=\"song\"]/location[.=\"${uri}\"]/..'); 
print res[0]; 
ctxt.xpathFreeContext(); doc.freeDoc()"

# Get artist and album
artist="The Calling"
album="Camino Palmero"

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
