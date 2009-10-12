/* Copyright 2009 Sean Hodges <seanhodges@bluebottle.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tesla.app.command.helper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class RhythmDBHelper implements ICommandHelper {
	
	public static final String MAGIC_MARKER = "[rhythmdb]";
	
	public String compileQuery(String uriCommand, boolean includeMarker) {
		StringBuilder builder = new StringBuilder();
		if (includeMarker) {
			builder.append("echo " + MAGIC_MARKER + ";");
		}
		builder.append("rhythmdb_path=~/.gnome2/rhythmbox/rhythmdb.xml; ");
		builder.append("if test -e ~/.local/share/rhythmbox/rhythmdb.xml; then ");
		builder.append(		"rhythmdb_path=~/.local/share/rhythmbox/rhythmdb.xml; ");
		builder.append("fi; ");
		builder.append("uri=\"$(" + uriCommand + " | sed -e \"s/'/\\\\\\'/g\")\"; ");
		builder.append("uri=\"${uri/method*string \\\"/}\"; ");
		builder.append("uri=\"${uri/\\\"/}\"; ");
		builder.append("if [[ ${uri} != \"\" ]]; then ");
		builder.append(		"python -c \"");
		builder.append(			"import libxml2;");
		builder.append(			"doc = libxml2.parseFile('${rhythmdb_path}');");
		builder.append(			"ctxt = doc.xpathNewContext();");
		builder.append(			"res = ctxt.xpathEval('//entry[@type=\\\"song\\\"]/location[.=\\\"${uri}\\\"]/..');");
		builder.append(			"print res[0];");
		builder.append(			"ctxt.xpathFreeContext(); doc.freeDoc()\"; ");
		builder.append("fi");
		return builder.toString();
	}
	
	public String compileQuery(String uriCommand) {
		return compileQuery(uriCommand, true);
	}
	
	public String getPlaylist(String uriCommand) {
		StringBuilder builder = new StringBuilder();
		builder.append("echo " + MAGIC_MARKER + ";");
		builder.append("rhythmdb_path=~/.gnome2/rhythmbox/rhythmdb.xml; if test -e ~/.local/share/rhythmbox/rhythmdb.xml; then rhythmdb_path=~/.local/share/rhythmbox/rhythmdb.xml; fi; ");
		builder.append("uri=\"$(" + uriCommand + " | sed -e \"s/'/\\\\\\'/g\")\"; ");
		builder.append("uri=\"${uri/method*string \\\"/}\"; ");
		builder.append("uri=\"${uri/\\\"/}\"; ");
		builder.append("if [[ ${uri} != \"\" ]]; then ");
		builder.append(	"python -c \"");
		builder.append(			"\nimport libxml2; ");
		builder.append(			"\ndoc = libxml2.parseFile('${rhythmdb_path}')"); 
		builder.append(			"\nctxt = doc.xpathNewContext()");
		builder.append(			"\ntrackData = ctxt.xpathEval('//entry[@type=\\\'song\\\']/location[.=\\\'${uri}\\\']/../*')");
		builder.append(			"\nfor child in trackData:");
		builder.append(			"\n		if child.name == 'artist':");
		builder.append(			"\n			artist = child.content");
		builder.append(			"\n		elif child.name == 'album':");
		builder.append(			"\n			album = child.content");
		builder.append(			"\nalbumData = []");
		builder.append(			"\nif artist != '' and album != '':");
		builder.append(			"\n		albumData = ctxt.xpathEval('//entry[@type=\\\'song\\\']/artist[.=\\\'' + artist + '\\\']/../album[.=\\\'' + album + '\\\']/..')");
		builder.append(			"\nprint '<playlist>'");
		builder.append(			"\nfor item in albumData:");
		builder.append(			"\n		print item");
		builder.append(			"\nprint '</playlist>'");
		builder.append(			"\nctxt.xpathFreeContext()");
		builder.append(			"\ndoc.freeDoc()");
		builder.append(	"\"; ");
		builder.append("fi");
		return builder.toString();
	}
	
	public String getSelectedPlaylistEntry() {
		StringBuilder builder = new StringBuilder();
		builder.append("echo " + MAGIC_MARKER + ";");
		builder.append("rhythmdb_path=~/.gnome2/rhythmbox/rhythmdb.xml; if test -e ~/.local/share/rhythmbox/rhythmdb.xml; then rhythmdb_path=~/.local/share/rhythmbox/rhythmdb.xml; fi; ");
		builder.append("uri=\"$(qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player getPlayingUri | sed -e \"s/'/\\\'/g\")\"; "); 
		builder.append("if [[ ${uri} != \"\" ]]; then ");
		builder.append(		"python -c \"");
		builder.append(			"import libxml2;  ");
		builder.append(			"doc = libxml2.parseFile('${rhythmdb_path}');  ");
		builder.append(			"ctxt = doc.xpathNewContext();  ");
		builder.append(			"res = ctxt.xpathEval('//entry[@type=\\\"song\\\"]/location[.=\\\"${uri}\\\"]/../track-number');  ");
		builder.append(			"print res[0]; "); 
		builder.append(			"ctxt.xpathFreeContext(); doc.freeDoc()\" ");
		builder.append("fi ");
		return builder.toString();
	}
	
	public Map<String, String> evaluateOutputAsMap(String rawOut) {
		Map<String, String> firstEntry = null;
		
		rawOut = rawOut.trim();
		if (rawOut.length() > 0) {
			XmlPlaylistParser contentHandler = new XmlPlaylistParser();
			XMLReader reader;
			try {
				InputSource is = new InputSource();
				is.setByteStream((InputStream)new ByteArrayInputStream(rawOut.getBytes("UTF-8")));
	            SAXParserFactory spf = SAXParserFactory.newInstance();
	            SAXParser sp = spf.newSAXParser();
	            reader = sp.getXMLReader(); 
				reader.setContentHandler(contentHandler);
				reader.parse(is);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (contentHandler.getOutput().size() > 0) {
				firstEntry = contentHandler.getOutput().get(0);
			}
		}
		
		return firstEntry;
	}

	public boolean evaluateOutputAsBoolean(String rawOut) {
		return false;
	}

	public String evaluateOutputAsString(String rawOut) {
		String out = "";
		if (rawOut != null && rawOut.length() > 0 && rawOut.indexOf("<track-number>") >= 0) {
			// Return the track number value
			out = out.substring("<track-number>".length() - 1, rawOut.length() - "</track-number>".length());
		}
		return out;
	}

	public List<String> evaluateOutputAsList(String rawOut) {
		List<String> out = new ArrayList<String>();
		
		rawOut = rawOut.trim().replaceAll("\n", "");
		if (rawOut.length() > 0) {
			XmlPlaylistParser contentHandler = new XmlPlaylistParser();
			XMLReader reader;
			try {
				InputSource is = new InputSource();
				is.setByteStream((InputStream)new ByteArrayInputStream(rawOut.getBytes("UTF-8")));
	            SAXParserFactory spf = SAXParserFactory.newInstance();
	            SAXParser sp = spf.newSAXParser();
	            reader = sp.getXMLReader(); 
				reader.setContentHandler(contentHandler);
				reader.parse(is);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (contentHandler.getOutput().size() > 0) {
				for (Map<String, String> entry : contentHandler.getOutput()) {
					if (entry.containsKey("title")) {
						out.add(entry.get("title"));
					}
				}
			}
		}
		
		return out;
	}
}
