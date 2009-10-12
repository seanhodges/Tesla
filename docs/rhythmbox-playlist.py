#!/usr/bin/env python

import libxml2
uri = "file:///media/data/Music/Radiohead/OK%20Computer/01%20Airbag.mp3"
doc = libxml2.parseFile('/home/sean/.local/share/rhythmbox/rhythmdb.xml')
ctxt = doc.xpathNewContext()
trackData = ctxt.xpathEval('//entry[@type=\"song\"]/location[.=\"' + uri + '\"]/../*')
for child in trackData:
	if child.name == "artist":
		artist = child.content
	elif child.name == "album":
		album = child.content

if artist != "" and album != "":
albumData = ctxt.xpathEval('//entry[@type="song"]/artist[.="' + artist + '"]/../album[.="' + album + '"]/..')
print "<playlist>"
for trackData in albumData: 
	print trackData
print "</playlist>"
ctxt.xpathFreeContext()
doc.freeDoc() 
