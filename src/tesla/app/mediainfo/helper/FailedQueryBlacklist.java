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

import java.util.ArrayList;
import java.util.List;

public class FailedQueryBlacklist {
	
	/*
	 * This is a temporary in-mem list of failed queries, to avoid flooding Last.FM with failed requests
	 * It will be redundant once the textual media info is being cached
	 * 
	 */
	
	public List<String> blacklist = null; 
	
	private static FailedQueryBlacklist instance = null;
	protected FailedQueryBlacklist() {}
	
	public static FailedQueryBlacklist getInstance() {
		if (instance == null) {
			instance = new FailedQueryBlacklist();
			instance.blacklist = new ArrayList<String>();
		}
		return instance;
	}
	
}
