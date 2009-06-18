package tesla.app.mediainfo;

import java.net.URL;

import tesla.app.mediainfo.helper.CacheStoreHelper;

public class MediaInfoFactory {

	public MediaInfo process(MediaInfo info) {
		CacheStoreHelper cache = new CacheStoreHelper();
		// Check the FS cache for matching covers
		String artwork = null;
		if (info.artist != null && info.album != null 
				&& info.artist.length() > 0 && info.album.length() > 0) { 
			artwork = cache.getArtworkPath(info.artist, info.album);
		}
		
		if (artwork == null) {
			// Retrieve cover from a provider to the cache (first successful query) 
			URL providerArtworkUrl = retrieveArtworkFromProvider(info);
			// Set cover path to new cache entry
			artwork = cache.copyArtworkFromUrl(info.artist, info.album, providerArtworkUrl);
		}
		info.artwork = artwork;
		
		// TODO: Process/cache the textual metadata
		
		return info;
	}
	
	public URL retrieveArtworkFromProvider(MediaInfo info) {
		// Traverse the providers until one finds a successful match
		// Save artwork to FS cache
		// Return the FS cache path
		return null;
	}

}
