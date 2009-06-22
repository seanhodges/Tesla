package tesla.app.mediainfo.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class CacheStoreHelper {

	private static final String ROOT_PATH = "/sdcard/albumthumbs/Tesla/";
	
	public String getArtworkPath(String artist, String album) {
		String filePath = null;
		// Build the directory structure if it does not already exist
		File root = new File(ROOT_PATH);
		if (!root.exists()) root.mkdirs();
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
	
	private File buildCachePath(String artist, String album) {
		artist = artist.replaceAll("\\W", "_");
		album = album.replaceAll("\\W", "_");
		return new File(ROOT_PATH + artist + "_" + album + ".jpg");
	}
}
