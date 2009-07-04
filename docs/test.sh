#!/bin/bash

echo $(qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player getPlayingUri | sed -e "s/'/\\\'/g")

python -c "import libxml2; doc = libxml2.parseFile('/home/sean/.local/share/rhythmbox/rhythmdb.xml'); ctxt = doc.xpathNewContext(); res = ctxt.xpathEval('//entry[@type=\"song\"]/location[.=\"$(qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player getPlayingUri | sed -e "s/'/\\\'/g")\"]/..'); print res[0]; ctxt.xpathFreeContext(); doc.freeDoc()"
