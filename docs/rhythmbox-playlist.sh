#!/bin/bash

echo [rhythmdb];
rhythmdb_path=~/.gnome2/rhythmbox/rhythmdb.xml; 
if test -e ~/.local/share/rhythmbox/rhythmdb.xml; then 
	rhythmdb_path=~/.local/share/rhythmbox/rhythmdb.xml; 
fi; 
uri="$(dbus-send --session --print-reply --dest=org.gnome.Rhythmbox --type="method_call" /org/gnome/Rhythmbox/Player org.gnome.Rhythmbox.Player.getPlayingUri | sed -e "s/'/\\\'/g")"; 
uri="${uri/method*string \"/}"; 
uri="${uri/\"/}"; 
if [[ ${uri} != "" ]]; then 
	python -c "
import libxml2; 
doc = libxml2.parseFile('${rhythmdb_path}')
ctxt = doc.xpathNewContext()
trackData = ctxt.xpathEval('//entry[@type=\'song\']/location[.=\'${uri}\']/../*')
for child in trackData:
		if child.name == 'artist':
			artist = child.content
		elif child.name == 'album':
			album = child.content
albumData = []
if artist != '' and album != '':
		albumData = ctxt.xpathEval('//entry[@type=\'song\']/artist[.=\'' + artist + '\']/../album[.=\'' + album + '\']/..')
print '<playlist>'
for item in albumData:
		print item
print '</playlist>'
ctxt.xpathFreeContext()
doc.freeDoc()"; 
fi
