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

package tesla.test.lastfm;

import java.net.URL;
import java.util.Collection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import net.roarsoftware.lastfm.Album;
import net.roarsoftware.lastfm.Artist;
import net.roarsoftware.lastfm.ImageSize;
import net.roarsoftware.lastfm.Track;

import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class GetCoverTest extends TestCase {
	
	private static final String LAST_FM_KEY = "ef451aaf87a01a1cc9d349dab20de89a";

	@Test
	public void testGetArtistTopTen() throws Exception {
		String artist = "Eminem";
		Collection<Track> topTracks = Artist.getTopTracks(artist, LAST_FM_KEY);
		assertNotNull(topTracks);
		//System.out.println("Top Tracks for " + artist + ":");
		for (Track track : topTracks) {
			assertNotSame(0, track.getName());
			//System.out.printf("%s (%d plays)%n", track.getName(), track.getPlaycount());
		}
	}
	
	@Test
	public void testGettingAlbumInformation() throws Exception {
		String artist = "Eminem";
		String albumOrMbid = "The Eminem Show";
		Album info = Album.getInfo(artist, albumOrMbid, LAST_FM_KEY);
		assertNotNull(info);
		String out = info.getImageURL(ImageSize.SMALL);
		assertNotNull(out);
		//System.out.println("Album cover: " + out);
	}
}
