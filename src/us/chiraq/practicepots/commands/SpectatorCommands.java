package us.chiraq.practicepots.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import us.chiraq.practicepots.profile.Profile;
import us.chiraq.practicepots.profile.ProfileManager;

public class SpectatorCommands implements CommandExecutor, TabCompleter {

    private ProfileManager pm;
    
    public SpectatorCommands(ProfileManager pm) {
    	this. pm = pm;
    }
    
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "/spec <player>");
			sender.sendMessage(ChatColor.RED + "/spec leave");
			return true;
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("leave")) {
				Player spec = (Player) sender;
				Profile s = Profile.getProfile(spec.getUniqueId());
				s.setSpectating(null);
				s.setInSpectator(false);
				pm.sendToSpawn(spec);
				return true;
			} else if (args[0].equalsIgnoreCase("test")) {
				Player player = (Player) sender;
				Profile pro = Profile.getProfile(player.getUniqueId());
				Bukkit.broadcastMessage(pro.getSpectatingPlayers().toString());
				return true;
			}
			Player fighter;
			if ((fighter = Bukkit.getPlayer(args[0])) != null) {
				Player spec = (Player) sender;
				Profile f = Profile.getProfile(fighter.getUniqueId());
				Profile s = Profile.getProfile(spec.getUniqueId());
				pm.spectatePlayer(fighter, spec, f, s);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "The player was not found!");
				return true;
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> toReturn = new ArrayList<String>();
        if (args.length == 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                toReturn.add(player.getName());
            }
        }
        return toReturn;
	}
	
}
