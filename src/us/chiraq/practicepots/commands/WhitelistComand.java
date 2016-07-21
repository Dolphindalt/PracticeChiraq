package us.chiraq.practicepots.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.chiraq.practicepots.profile.Profile;

public class WhitelistComand implements CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if (!sender.hasPermission("practice.premium")) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
			return true;
		}
		if (args.length == 0) {
			sender.sendMessage(ChatColor.YELLOW + "Duel White List");
			Profile p = Profile.getProfile(((Player)sender).getUniqueId());
			for (UUID s : p.getDuelWhiteList()) {
				sender.sendMessage(ChatColor.AQUA + Bukkit.getOfflinePlayer(s).getName());
			}
			return true;
		} else if (args.length == 1) {
			OfflinePlayer w;
			if ((w = Bukkit.getOfflinePlayer(args[0])) != null) {
				Profile p = Profile.getProfile(((Player)sender).getUniqueId());
				if (p.getDuelWhiteList().contains(w.getUniqueId())) {
					p.removePlayerfromWhitelist(w);
					sender.sendMessage(ChatColor.GREEN + "Removed " + w.getName() + " from the duel white list!");
				} else {
					p.addPlayertoWhitelist(w);
					sender.sendMessage(ChatColor.GREEN + "Added " + w.getName() + " to the duel white list!");
				}
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Player not found!");
			}
			return true;
		}
		return true;
	}


	
	
}
