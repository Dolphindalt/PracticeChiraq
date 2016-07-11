package us.chiraq.practicepots.commands;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Arena;
import us.chiraq.practicepots.game.Ladder;
import us.chiraq.practicepots.game.kit.Kit;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LadderCommand
implements CommandExecutor {
    private Nanny main = Nanny.getInstance();
    private LangFile lf = this.main.getLangFile();

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("staff.admin")) {
            sender.sendMessage(this.lf.getString("NO_PERMISSION"));
            return true;
        }
        if (args.length == 1 && args[0].equals("list")) {
            if (Ladder.getLadders().isEmpty()) {
                sender.sendMessage(this.lf.getString("LADDER_COMMAND.NO_LADDERS"));
                return true;
            }
            String ladders = "";
            ChatColor color = ChatColor.valueOf((String)this.lf.getString("LADDER_COMMAND.LIST_COLOR"));
            for (Ladder ladder : Ladder.getLadders()) {
                ladders = ladders + (Object)color + ladder.getName() + (Object)ChatColor.valueOf((String)this.lf.getString("LADDER_COMMAND.LIST_COMMA_COLOR")) + ", ";
            }
            ladders = ladders.substring(0, ladders.length() - 2);
            for (String message : this.lf.getStringList("LADDER_COMMAND.LIST")) {
                sender.sendMessage(message.replace("%LADDERS%", ladders));
            }
            return true;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setkit")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(this.lf.getString("NOT_PLAYER"));
                    return true;
                }
                Player player = (Player)sender;
                Ladder ladder = Ladder.getLadder(args[1]);
                if (ladder == null) {
                    sender.sendMessage(this.lf.getString("LADDER_COMMAND.NO_LADDER_FOUND"));
                    return true;
                }
                Kit kit = new Kit(ladder, player.getInventory().getArmorContents(), player.getInventory().getContents());
                kit.setName("Default " + ladder.getName() + " Kit");
                ladder.setKit(kit);
                player.sendMessage(this.lf.getString("LADDER_COMMAND.KIT_SET"));
                return true;
            }
            if (args[0].equalsIgnoreCase("listarenas")) {
                Ladder ladder = Ladder.getLadder(args[1]);
                if (ladder == null) {
                    sender.sendMessage(this.lf.getString("LADDER_COMMAND.NO_LADDER_FOUND"));
                    return true;
                }
                if (ladder.getArenas().isEmpty()) {
                    sender.sendMessage(this.lf.getString("ARENA_COMMAND.NO_ARENAS"));
                    return true;
                }
                String arenas = "";
                ChatColor color = ChatColor.valueOf((String)this.lf.getString("ARENA_COMMAND.LIST_COLOR"));
                for (Arena arena : ladder.getArenas()) {
                    arenas = arenas + (Object)color + arena.getId() + (Object)ChatColor.valueOf((String)this.lf.getString("ARENA_COMMAND.LIST_COMMA_COLOR")) + ", ";
                }
                arenas = arenas.substring(0, arenas.length() - 2);
                for (String message : this.lf.getStringList("ARENA_COMMAND.LIST")) {
                    sender.sendMessage(message.replace("%ARENAS%", arenas));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("kit")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(this.lf.getString("NOT_PLAYER"));
                    return true;
                }
                Player player = (Player)sender;
                Ladder ladder = Ladder.getLadder(args[1]);
                if (ladder == null) {
                    sender.sendMessage(this.lf.getString("LADDER_COMMAND.NO_LADDER_FOUND"));
                    return true;
                }
                if (ladder.getKit() == null) {
                    sender.sendMessage(this.lf.getString("LADDER_COMMAND.NO_KIT"));
                    return true;
                }
                player.sendMessage(this.lf.getString("LADDER_COMMAND.KIT_RECEIVED"));
                player.getInventory().setArmorContents(ladder.getKit().getArmor());
                player.getInventory().setContents(ladder.getKit().getInventory());
                player.updateInventory();
                return true;
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("addarena")) {
                Ladder ladder = Ladder.getLadder(args[2]);
                if (ladder == null) {
                    sender.sendMessage(this.lf.getString("LADDER_COMMAND.NO_LADDER_FOUND"));
                    return true;
                }
                Arena arena = Arena.getArena(args[1]);
                if (arena == null) {
                    sender.sendMessage(this.lf.getString("ARENA_COMMAND.NO_ARENA_FOUND"));
                    return true;
                }
                if (ladder.getArenas().contains(arena)) {
                    sender.sendMessage("LADDER_COMMAND.ARENA_ALREADY_IN");
                    return true;
                }
                ladder.getArenas().add(arena);
                sender.sendMessage(this.lf.getString("LADDER_COMMAND.ARENA_ADDED"));
                return true;
            }
            if (args[0].equalsIgnoreCase("removearena")) {
                Ladder ladder = Ladder.getLadder(args[2]);
                if (ladder == null) {
                    sender.sendMessage(this.lf.getString("LADDER_COMMAND.NO_LADDER_FOUND"));
                    return true;
                }
                Arena arena = Arena.getArena(args[1]);
                if (arena == null) {
                    sender.sendMessage(this.lf.getString("ARENA_COMMAND.NO_ARENA_FOUND"));
                    return true;
                }
                if (!ladder.getArenas().contains(arena)) {
                    sender.sendMessage(this.lf.getString("LADDER_COMMAND.ARENA_NOT_IN"));
                    return true;
                }
                ladder.getArenas().remove(arena);
                sender.sendMessage(this.lf.getString("LADDER_COMMAND.ARENA_REMOVED"));
                return true;
            }
        }
        for (String message : this.lf.getStringList("LADDER_COMMAND.USAGE")) {
            sender.sendMessage(message);
        }
        return true;
    }
}

