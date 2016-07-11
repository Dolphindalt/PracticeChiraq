package us.chiraq.practicepots.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.profile.Profile;

public class SettingsCommand implements CommandExecutor {

    private Nanny main = Nanny.getInstance();
    private LangFile lf = this.main.getLangFile();
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage("Settings");
			sender.sendMessage("visability or v");
			return true;
		}
		
		if (args.length < 0) {
			if (args[0].equalsIgnoreCase("visibility") || args[0].equalsIgnoreCase("v")) {
				Player player = (Player)sender;
				Profile pro = Profile.getProfile(player.getUniqueId());
				if (pro.isShowPlayers()) {
					pro.setShowPlayers(false);
				} else {
					pro.setShowPlayers(true);
				}
				sender.sendMessage(this.lf.getString("SETTINGS.MESSAGES.PLAYER_VISIBILITY").replace("%VALUE%", pro.isShowPlayers() + ""));
			}
		}
		sender.sendMessage((Object)ChatColor.RED + "You lack the correct permissions to use this command!");
		return true;
	}

}
