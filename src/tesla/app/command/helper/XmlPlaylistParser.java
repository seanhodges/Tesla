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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlPlaylistParser extends DefaultHandler {

	private String mode = "";
	private static final String TRACKNO = "tracknumber";
	private static final String TITLE = "title";
	private static final String ARTIST = "artist";
	private static final String ALBUM = "album";
	private static final String LENGTH = "mtime";
	
	private Map<String, String> out = new HashMap<String, String>();
	private List<Map<String, String>> outList = new ArrayList<Map<String, String>>();
	
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		
		if (localName.equalsIgnoreCase("track-number")) {
			mode = TRACKNO;
		}
		else if (localName.equalsIgnoreCase("title")) {
			mode = TITLE;
		}
		else if (localName.equalsIgnoreCase("artist")) {
			mode = ARTIST;
		}
		else if (localName.equalsIgnoreCase("album")) {
			mode = ALBUM;
		}
		else if (localName.equalsIgnoreCase("mtime")) {
			mode = LENGTH;
		}
		else {
			mode = "";
		}
	}
	
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		if (!mode.equals("")) {
			out.put(mode, new String(ch, start, length));
		}
	}

	public void endElement(String uri, String localName, String name)
			throws SAXException {
		super.endElement(uri, localName, name);
		if (localName.equals("item") || localName.equals("entry")) {
			outList.add(out);
			out = new HashMap<String, String>();
		}
		mode = "";
	}
	
	public List<Map<String, String>> getOutput() {
		return outList;
	}
}
