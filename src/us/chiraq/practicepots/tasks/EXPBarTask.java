package us.chiraq.practicepots.tasks;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import us.chiraq.practicepots.profile.Profile;

public class EXPBarTask extends BukkitRunnable {

	private Player player;
	private Profile profile;
	private int level;
	
	public EXPBarTask(Player player, Profile profile) {
		this.player = player;
		this.profile = profile;
		this.level = 16;
	}
	
	public void run() {
		if (level != 0 && profile.getDuel() != null) {
			player.setLevel(player.getLevel()-1);
			level--;
		} else {
			this.cancel();
		}
	}
	
}
