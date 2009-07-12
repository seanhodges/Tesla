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

package tesla.app.mediainfo.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class CacheStoreHelper {

	private static final String ROOT_PATH = "/sdcard/albumthumbs/Tesla/";
	
	public CacheStoreHelper() {
		// Build the directory structure if it does not already exist
		File root = new File(ROOT_PATH);
		if (!root.exists()) root.mkdirs();
	}
	
	public String getArtworkPath(String artist, String album) {
		String filePath = null;
		File test = buildCachePath(artist, album);
		if (test.exists()) filePath = test.getAbsolutePath();
		return filePath;
	}

	public String copyArtworkFromUrl(String artist, String album, URL providerArtwork) {
		File filePath = buildCachePath(artist, album);

		try {
			@SuppressWarnings("unused")
			URLConnection connection = providerArtwork.openConnection();
			InputStream is = providerArtwork.openStream();
			 
			FileOutputStream os = new FileOutputStream(filePath);
			int theChar;
			while ((theChar = is.read()) != -1) {
				os.write(theChar);
			}
			
			os.close();
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return filePath.getAbsolutePath();
	}
	
	public File buildCachePath(String artist, String album) {
		artist = artist.replaceAll("\\W", "_");
		album = album.replaceAll("\\W", "_");
		return new File(ROOT_PATH + artist + "_" + album + ".jpg");
	}
}
