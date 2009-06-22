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

import tesla.app.mediainfo.MediaInfo;
import tesla.app.mediainfo.helper.CacheStoreHelper;

public class CacheProvider implements IMediaInfoProvider {

	public boolean populate(MediaInfo info) {
		CacheStoreHelper cache = new CacheStoreHelper();
		String artwork = cache.getArtworkPath(info.artist, info.album);
		if (artwork != null) {
			info.artwork = artwork;
			return true;
		}
		return false;
	}
}
