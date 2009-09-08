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
	
	public Map<String, String> evaluateOutputAsMap(String rawOut) {
		
		rawOut = rawOut.trim();
		
		RhythmDBParser contentHandler = new RhythmDBParser();
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
		
		return contentHandler.getOutput();
	}

	public boolean evaluateOutputAsBoolean(String rawOut) {
		return false;
	}

	public String evaluateOutputAsString(String rawOut) {
		return null;
	}
}
