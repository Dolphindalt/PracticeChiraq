package us.chiraq.practicepots.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Ladder;
import us.chiraq.practicepots.profile.Profile;
import us.chiraq.practicepots.utils.Data;

public class LeaderboardCommands implements CommandExecutor {

    private Nanny main = Nanny.getInstance();
    private LangFile lf = this.main.getLangFile();
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(this.lf.getString("NOT_PLAYER"));
			return true;
		}
		Player p = (Player) sender;
		if (args.length == 0) {
			sender.sendMessage("Leaderboards");
			sender.sendMessage("Global");
			for (Ladder l : Ladder.getLadders()) {
				sender.sendMessage(l.getName().replace(" ", ""));
			}
			return true;
		}
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("global"))
			{
				Data.showGlobalLeaderboard(p, Profile.getProfile(p.getUniqueId()), -1);
			} else {
				for (Ladder l : Ladder.getLadders()) {
					if (l.getName().replace(" ", "").equalsIgnoreCase(args[0])) {
						Data.showLadderLeaderboard(p, Profile.getProfile(p.getUniqueId()), l, -1);
						return true;
					}
				}
				sender.sendMessage(ChatColor.RED + "The leaderboard title you entered does not exist! Type /lb for leaderboard titles!");
				return true;
			}
			return true;
		}
		if (args.length == 2) {
			
			if (args[0].equalsIgnoreCase("global"))
			{
				int i;
				if ((i = Integer.parseInt(args[1])) != -1) {
				Data.showGlobalLeaderboard(p, Profile.getProfile(p.getUniqueId()), i);
				} else {
					Data.showGlobalLeaderboard(p, Profile.getProfile(p.getUniqueId()), -1);
				}
				return true;
			} else {
				for (Ladder l : Ladder.getLadders()) {
					if (l.getName().replace(" ", "").equalsIgnoreCase(args[0])) {
						int i;
						if ((i = Integer.parseInt(args[1])) != -1) {
							Data.showLadderLeaderboard(p, Profile.getProfile(p.getUniqueId()), l, i);
							return true;
						} else {
							Data.showLadderLeaderboard(p, Profile.getProfile(p.getUniqueId()), l, -1);
						}
						return true;
					}
				}
				sender.sendMessage(ChatColor.RED + "The leaderboard title you entered does not exist! Type /lb for leaderboard titles!");
				return true;
			}
		}
		return true;
	}

	
	
}
