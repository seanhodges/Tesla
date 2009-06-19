package tesla.app.mediainfo.helper;

import java.io.File;
import java.net.URL;

public class CacheStoreHelper {

	private static final String ROOT_PATH = "/sdcard/albumthumbs/Tesla/";
	
	public String getArtworkPath(String artist, String album) {
		String filePath = null;
		File test = buildCachePath(artist, album);
		if (test.exists()) filePath = test.getAbsolutePath();
		return filePath;
	}

	public String copyArtworkFromUrl(String artist, String album, URL providerArtwork) {
		File filePath = buildCachePath(artist, album);
		
		// TODO: Copy the contents at the URL to the new cache path
		
		return filePath.getAbsolutePath();
	}
	
	private File buildCachePath(String artist, String album) {
		artist = artist.replaceAll("\\W", "_");
		album = album.replaceAll("\\W", "_");
		return new File(ROOT_PATH + artist + "_" + album + ".jpg");
	}
}
