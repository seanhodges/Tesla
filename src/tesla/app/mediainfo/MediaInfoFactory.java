package tesla.app.mediainfo;

import java.util.ArrayList;
import java.util.Iterator;

import tesla.app.mediainfo.helper.CacheStoreHelper;
import tesla.app.mediainfo.helper.FailedQueryBlacklist;
import tesla.app.mediainfo.provider.CacheProvider;
import tesla.app.mediainfo.provider.IMediaInfoProvider;
import tesla.app.mediainfo.provider.LastfmProvider;

public class MediaInfoFactory {

	ArrayList<IMediaInfoProvider> providerScanner = new ArrayList<IMediaInfoProvider>();
	
	public MediaInfoFactory() {
		IMediaInfoProvider cacheProvider = new CacheProvider();
		IMediaInfoProvider lastfmProvider = new LastfmProvider();
		
		// Scan the config providers in this order
		providerScanner.add(cacheProvider);
		providerScanner.add(lastfmProvider);
	}
	
	public MediaInfo process(MediaInfo info) {
		// TODO: Currently requires an artist AND album for last.fm provider to work
		if (info.artist != null && info.album != null
				&& !info.artist.equals("") && !info.album.equals("")) {
			boolean success = false;
			Iterator<IMediaInfoProvider> providerOrderIt = providerScanner.iterator();
			while (!success && providerOrderIt.hasNext()) {
				IMediaInfoProvider currentProvider = providerOrderIt.next(); 
				try {
					success = currentProvider.populate(info);
				}
				catch (Exception e) {
					success = false;
				}
			}
			
			if (success == false) {
				// Add failed request to blacklist
				FailedQueryBlacklist.getInstance().blacklist.add(
						new CacheStoreHelper().buildCachePath(info.artist, info.album).getAbsolutePath());
			}
		}
		
		return info;
	}
}
