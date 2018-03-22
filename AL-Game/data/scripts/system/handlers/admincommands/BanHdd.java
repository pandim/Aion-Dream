/**
 * This file is part of Aion-Lightning <aion-lightning.org>.
 *
 *  Aion-Lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Aion-Lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details. *
 *  You should have received a copy of the GNU General Public License
 *  along with Aion-Lightning.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package admincommands;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.BannedHDDManager;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Alex
 */
public class BanHdd extends AdminCommand {

	public BanHdd() {
		super("banhdd");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			onFail(player, "Please add one or more parameters");
			return;
		}

		int time;
		String hdd_serial;
		String targetName = "direct_type";

		// try parsing
		try {
			time = Integer.parseInt(params[0]);

			if (time == 0) // 0 is 10 years since system don't allow infinte banns without rework - it's pseudo infinity
			{
				time = 60 * 24 * 365 * 10;
			}
		}
		catch (NumberFormatException e) {
			onFail(player, "Please enter a valid integer amount of minutes");
			return;
		}

		// is mac defined?
		if (params.length > 1) {
			hdd_serial = params[1];
		}
		else { // no address defined
			VisibleObject target = player.getTarget();
			if (target != null && target instanceof Player) {
				if (target.getObjectId().equals(player.getObjectId())) {
					onFail(player, "Omg, disselect yourself please.");
					return;
				}

				Player targetpl = (Player) target;
				hdd_serial = targetpl.getClientConnection().getHddSerial();
				targetName = targetpl.getName();
				targetpl.getClientConnection().closeNow();
			}
			else {
				onFail(player, "You should select a player or give me any hdd serial");
				return;
			}
		}

		BannedHDDManager.getInstance().banAddress(hdd_serial, System.currentTimeMillis() + time * 60 * 1000, "author=" + player.getName() + ", " + player.getObjectId() + "; target=" + targetName);
	}

	@Override
	public void onFail(Player player, String message) {
		if (!message.isEmpty()) {
			PacketSendUtility.sendMessage(player, message);
		}
		PacketSendUtility.sendMessage(player, "Syntax: //banhdd [time in minutes] <hdd_serial>");
		PacketSendUtility.sendMessage(player, "Note: 0 minutes will cause permanent ban");
	}

}
