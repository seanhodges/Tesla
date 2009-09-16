xml_path=$(dcop --all-users amarok playlist saveCurrentPlaylist); 
if [[ ${xml_path} != "" ]]; then 
python -c "
import libxml2; doc = libxml2.parseFile('${xml_path}'); ctxt = doc.xpathNewContext(); res = ctxt.xpathEval('/playlist/item');
for track in res:
    print track;
ctxt.xpathFreeContext(); doc.freeDoc()"; 
fi
