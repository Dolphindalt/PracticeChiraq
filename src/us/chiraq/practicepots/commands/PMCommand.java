package us.chiraq.practicepots.commands;

import net.rymate.bchatmanager.bChatManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.chiraq.practicepots.files.types.PMFile;
import us.chiraq.practicepots.profile.Profile;
import us.chiraq.practicepots.utils.Items;

public class PMCommand implements CommandExecutor {

	private String fromformat;
	private String toformat;
	private String tag;
	
	public PMCommand(PMFile pmfile) {
		this.toformat = pmfile.getString("FORMAT_TO");
		this.fromformat = pmfile.getString("FORMAT_FROM");
		this.tag = pmfile.getString("TAG_FORMAT");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0 || args.length == 1) {
			sender.sendMessage(ChatColor.RED + "/pm <player> <message>");
			return true;
		} else if (args.length > 1) {
			Player other;
			if ((other = Bukkit.getPlayer(args[0])) != null) {
				Player player = (Player) sender;
				Profile p = Profile.getProfilefromOnline(player.getUniqueId());
				Profile o = Profile.getProfilefromOnline(other.getUniqueId());
				
				String message = "";
				for (int i = 1; i < args.length; i++) {
					message = message + " " + args[i];
				}
				
				String playerTag = getPlayerTag(player, this.tag);
				String otherTag = getPlayerTag(other, this.tag);
				
				String from = Items.addChatColor(formatToString(o.getChatcolor(), playerTag, this.toformat) + message);
				String to = Items.addChatColor(formatFromString(p.getChatcolor(), otherTag, this.fromformat) + message);
				player.sendMessage(to);
				other.sendMessage(from);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Player not found!");
				return true;
			}
		}
		return true;
	}

	public String getPlayerTag(Player player, String tag) {
		return tag
				.replace("%TAG%", bChatManager.chat.getPlayerPrefix(player))
				.replace("%PLAYER%", player.getDisplayName());
	}
	
	public String formatToString(String chatcolor, String player, String format) {
		return format
				.replace("%C%", chatcolor)
				.replace("%PLAYER%", player);
	}
	
	public String formatFromString(String chatcolor, String player, String format) {
		return format
				.replace("%C%", chatcolor)
				.replace("%PLAYER%", player);
	}
	
}
