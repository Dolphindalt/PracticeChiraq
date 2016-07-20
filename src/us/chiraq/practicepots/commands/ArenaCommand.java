package us.chiraq.practicepots.commands;

import java.util.Iterator;
import java.util.List;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Arena;
import us.chiraq.practicepots.game.Ladder;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommand
implements CommandExecutor {
    private Nanny main = Nanny.getInstance();
    private LangFile lf = this.main.getLangFile();

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("staff.admin")) {
            sender.sendMessage(this.lf.getString("NO_PERMISSION"));
            return true;
        }
        if (args.length == 1 && args[0].equals("list")) {
            if (Arena.getArenas().isEmpty()) {
                sender.sendMessage(this.lf.getString("ARENA_COMMAND.NO_ARENAS"));
                return true;
            }
            String arenas = "";
            ChatColor color = ChatColor.valueOf((String)this.lf.getString("ARENA_COMMAND.LIST_COLOR"));
            for (Arena arena : Arena.getArenas()) {
                arenas = arenas + (Object)color + arena.getId() + (Object)ChatColor.valueOf((String)this.lf.getString("ARENA_COMMAND.LIST_COMMA_COLOR")) + ", ";
            }
            arenas = arenas.substring(0, arenas.length() - 2);
            for (String message : this.lf.getStringList("ARENA_COMMAND.LIST")) {
                sender.sendMessage(message.replace("%ARENAS%", arenas));
            }
            return true;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setloc1")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(this.lf.getString("NOT_PLAYER"));
                    return true;
                }
                Player player = (Player)sender;
                Arena arena = Arena.getArena(args[1]);
                if (arena == null) {
                    sender.sendMessage(this.lf.getString("ARENA_COMMAND.NO_ARENA_FOUND"));
                    return true;
                }
                arena.getSpawnLocations()[0] = player.getLocation();
                player.sendMessage(this.lf.getString("ARENA_COMMAND.LOCATION_ONE_SET"));
                return true;
            }
            if (args[0].equalsIgnoreCase("setloc2")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(this.lf.getString("NOT_PLAYER"));
                    return true;
                }
                Player player = (Player)sender;
                Arena arena = Arena.getArena(args[1]);
                if (arena == null) {
                    sender.sendMessage(this.lf.getString("ARENA_COMMAND.NO_ARENA_FOUND"));
                    return true;
                }
                arena.getSpawnLocations()[1] = player.getLocation();
                player.sendMessage(this.lf.getString("ARENA_COMMAND.LOCATION_TWO_SET"));
                return true;
            }
            if (args[0].equalsIgnoreCase("delete")) {
                Arena arena = Arena.getArena(args[1]);
                if (arena == null) {
                    sender.sendMessage(this.lf.getString("ARENA_COMMAND.NO_ARENA_FOUND"));
                    return true;
                }
                Arena.getArenas().remove(arena);
                
                Iterator<Ladder> itr = Ladder.getLadders().iterator();
                while (itr.hasNext()) {
                	Ladder l = itr.next();
                	for (Arena a : l.getArenas()) {
                		if (a.equals(arena)) {
                			List<Arena> newA = l.getArenas();
                			newA.remove(arena);
                			l.setArenas(newA);
                		}
                	}
                }
                
                sender.sendMessage(this.lf.getString("ARENA_COMMAND.DELETED"));
                return true;
            }
            if (args[0].equalsIgnoreCase("create")) {
                Arena arena = Arena.getArena(args[1]);
                if (arena != null) {
                    sender.sendMessage(this.lf.getString("ARENA_COMMAND.ARENA_EXISTS"));
                    return true;
                }
                new Arena(args[1]);
                sender.sendMessage(this.lf.getString("ARENA_COMMAND.CREATED"));
                return true;
            }
        }
        for (String message : this.lf.getStringList("ARENA_COMMAND.USAGE")) {
            sender.sendMessage(message);
        }
        return true;
    }
}

