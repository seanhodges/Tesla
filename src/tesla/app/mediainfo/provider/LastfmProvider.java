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

package tesla.app.mediainfo.provider;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import net.roarsoftware.lastfm.Album;
import net.roarsoftware.lastfm.Caller;
import net.roarsoftware.lastfm.ImageSize;
import net.roarsoftware.lastfm.cache.MemoryCache;

import tesla.app.mediainfo.MediaInfo;
import tesla.app.mediainfo.helper.CacheStoreHelper;

public class LastfmProvider implements IMediaInfoProvider {
	
	private static final String DEFAULT_API_ROOT = "http://ws.audioscrobbler.com/2.0/";
	private static final String LAST_FM_KEY = "ef451aaf87a01a1cc9d349dab20de89a";
	
	public LastfmProvider() {
		// Cache to memory, as we have our own caching mechanism
		Caller.getInstance().setCache(new MemoryCache());
	}
	
	public boolean populate(MediaInfo info) {
		boolean reachable = false;
		try {
			InetAddress dnsQuery = InetAddress.getByName("ws.audioscrobbler.com");
			if (dnsQuery != null) {
				reachable = dnsQuery.isReachable(3000);
			}
		} catch (Exception e) {
			reachable = false;
		}
		if (reachable) {
			Album albumData = Album.getInfo(info.artist, info.album, LAST_FM_KEY);
			String artworkUrl = albumData.getImageURL(ImageSize.MEDIUM);
			
			if (artworkUrl != null) {
				CacheStoreHelper cache = new CacheStoreHelper();
				try {
					String artwork = cache.copyArtworkFromUrl(info.artist, info.album, new URL(artworkUrl));
					if (artwork != null) {
						info.artwork = artwork;
						return true;
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
		// Retrieval failed
		return false;
	}
}
