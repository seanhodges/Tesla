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

package tesla.app.service.connect;

import tesla.app.command.Command;
import tesla.app.command.provider.AppConfigProvider;

public class FakeConnection implements IConnection {

	public void connect(ConnectionOptions config) throws ConnectionException {
		// Do nothing
	}

	public void disconnect() {
		// Do nothing
	}

	public boolean isConnected() {
		return true;
	}

	public String sendCommand(Command command) throws ConnectionException {
		if (command.getKey().equals(Command.INIT)) {
			return "success\n";
		}
		else if (command.getKey().equals(Command.VOL_CURRENT)) {
			String out = "";
			if (command.getTargetApp().equals(AppConfigProvider.APP_RHYTHMBOX)) {
				out = "[dbus]\nmethod return sender=:1.66 -> dest=:1.1267 reply_serial=2\n   double 0.5";
			}
			else {
				out = "[dbus]\nmethod return sender=:1.66 -> dest=:1.1267 reply_serial=2\n   int32 50";
			}
			return out;
		}
		else if (command.getKey().equals(Command.GET_MEDIA_INFO)) {
			if (command.getTargetApp().equals(AppConfigProvider.APP_RHYTHMBOX)) {
				return "[rhythmdb]\n" + 
					"<entry type=\"song\">" +
				    "<title>Perfect Symmetry</title>" +
				    "<genre>Unknown</genre>" +
				    "<artist>Keane</artist>" +
				    "<album>Perfect Symmetry</album>" +
				    "<track-number>5</track-number>" +
				    "<duration>312</duration>" +
				    "<file-size>6057394</file-size>" +
				    "<location>file:///home/sean/Music/Keane/Perfect%20Symmetry/05%20-%20Perfect%20Symmetry.ogg</location>" +
				    "<mountpoint>file:///</mountpoint>" +
				    "<mtime>1224489723</mtime>" +
				    "<first-seen>1224489724</first-seen>" +
				    "<last-seen>1247218028</last-seen>" +
				    "<play-count>12</play-count>" +
				    "<last-played>1246918067</last-played>" +
				    "<bitrate>160</bitrate>" +
				    "<date>733325</date>" +
				    "<mimetype>application/ogg</mimetype>" +
				    "<mb-trackid>67f5d228-2d37-4e5a-8339-2044cd0df26b</mb-trackid>" +
				    "<mb-artistid>c7020c6d-cae9-4db3-92a7-e5c561cbad50</mb-artistid>" +
				    "<mb-albumid>13812b86-90ca-4bf2-9818-168fad7be13e</mb-albumid>" +
				    "<mb-albumartistid>c7020c6d-cae9-4db3-92a7-e5c561cbad50</mb-albumartistid>" +
				    "<mb-artistsortname>Keane</mb-artistsortname>" +
				    "</entry>";
			}
			else {
				return "[dbus]\n" +
					"dict entry(\n variant tracknumber:\n variant int32 1\n)\n" +
					"dict entry(\n variant title:\n variant string \"Karma Police\"\n)\n" +
					"dict entry(\n variant artist:\n variant string \"Radiohead\"\n)\n" +
					"dict entry(\n variant album:\n variant string \"OK Computer\"\n)\n";
			}
		}
		else if (command.getKey().equals(Command.IS_PLAYING)) {
			return "[dbus]\nmethod return sender=:1.66 -> dest=:1.1267 reply_serial=2\n   boolean true";
		}
		else {
			return "";
		}
	}

}
