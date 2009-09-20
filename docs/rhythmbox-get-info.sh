#!/bin/bash

#echo $(qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player getPlayingUri | sed -e "s/'/\\\'/g")

rhythmdb_path=~/.gnome2/rhythmbox/rhythmdb.xml; if test -e ~/.local/share/rhythmbox/rhythmdb.xml; then rhythmdb_path=~/.local/share/rhythmbox/rhythmdb.xml; fi; uri="$(qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player getPlayingUri | sed -e "s/'/\\\'/g")"; 
if [[ ${uri} != "" ]]; then
python -c "
import libxml2; 
doc = libxml2.parseFile('${rhythmdb_path}'); 
ctxt = doc.xpathNewContext(); 
res = ctxt.xpathEval('//entry[@type=\"song\"]/location[.=\"${uri}\"]/..'); 
print res[0]; 
ctxt.xpathFreeContext(); doc.freeDoc()"
fi
