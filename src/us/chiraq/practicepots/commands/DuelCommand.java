package us.chiraq.practicepots.commands;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.ConfigFile;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Arena;
import us.chiraq.practicepots.game.Ladder;
import us.chiraq.practicepots.game.fight.Duel;
import us.chiraq.practicepots.profile.Profile;
import us.chiraq.practicepots.utils.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class DuelCommand
implements CommandExecutor,
TabCompleter {
    private static Nanny main = Nanny.getInstance();
    private static LangFile lf = DuelCommand.main.getLangFile();
    private static ConfigFile cf = main.getConfigFile();

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(DuelCommand.lf.getString("NOT_PLAYER"));
            return true;
        }
        if (args.length == 1) {
            Player player = (Player)sender;
            if (player.getName().equalsIgnoreCase(args[0])) {
                player.sendMessage(DuelCommand.lf.getString("NOPE"));
                return true;
            }
            Profile profile = Profile.getProfile(player.getUniqueId());
            Player challenged = Bukkit.getPlayer((String)args[0]);
            if (challenged == null) {
                player.sendMessage(DuelCommand.lf.getString("NOT_FOUND"));
                return true;
            }
            if (profile.isInSpectator()) {
            	sender.sendMessage(DuelCommand.lf.getString("SPECTATOR.IN_SPECTATOR_MESSAGE"));
            	return true;
            }
            Inventory inventory = this.duelPlayer(player, challenged);
            profile.setDuelInventory(inventory);
            profile.setDuelPlayer(challenged);
            player.openInventory(inventory);
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("accept")) {
            Player player = (Player)sender;
            Profile profile = Profile.getProfile(player.getUniqueId());
            if (profile.isInSpectator()) {
            	sender.sendMessage(DuelCommand.lf.getString("SPECTATOR.IN_SPECTATOR_MESSAGE"));
            	return true;
            }
            if (player.getName().equalsIgnoreCase(args[1])) {
                player.sendMessage(DuelCommand.lf.getString("NOPE"));
                return true;
            }
            Player challenged = Bukkit.getPlayer((String)args[1]);
            if (challenged == null) {
                player.sendMessage(DuelCommand.lf.getString("NOT_FOUND"));
                return true;
            }
            Profile challengedProfile = Profile.getProfile(challenged.getUniqueId());
            if (challengedProfile.getTeam() != null) {
                player.sendMessage(DuelCommand.lf.getString("NOPE"));
                return true;
            }
            if (profile.getTeam() != null) {
                player.sendMessage(DuelCommand.lf.getString("NOPE"));
                return true;
            }
            if (!profile.getDuelRequests().containsKey((Object)challenged)) {
                player.sendMessage(DuelCommand.lf.getString("DUEL_COMMAND.NOT_VALID"));
                return true;
            }
            if (!challengedProfile.isInSpawn() || !profile.isInSpawn()) {
                player.sendMessage(DuelCommand.lf.getString("NOT_IN_SPAWN"));
                return true;
            }
            if (profile.getQueue() != null) {
                profile.getQueue().cancel();
                profile.getSearchingLadder().getUnrankedQueue().remove((Object)player);
                profile.getSearchingLadder().getRankedQueue().remove((Object)player);
                player.sendMessage(DuelCommand.lf.getString("QUEUE.CANCEL"));
            }
            if (challengedProfile.getQueue() != null) {
                challengedProfile.getQueue().cancel();
                challengedProfile.getSearchingLadder().getUnrankedQueue().remove((Object)player);
                challengedProfile.getSearchingLadder().getRankedQueue().remove((Object)player);
                challenged.sendMessage(DuelCommand.lf.getString("QUEUE.CANCEL"));
            }
            if (!challenged.hasPermission("practice.premium")) {
            	Ladder ladder = profile.getDuelRequests().get((Object)challenged);
            	new Duel(player, challenged, profile, challengedProfile, ladder, ladder.getArenas().get(new Random().nextInt(ladder.getArenas().size())), 1);
            } else {
            	new Duel(player, challenged, profile, challengedProfile, challengedProfile.getSelected(), challengedProfile.getArena(), 1);
            }
            return true;
        }
        for (String message : DuelCommand.lf.getStringList("DUEL_COMMAND.USAGE")) {
            sender.sendMessage(message);
        }
        return true;
    }

    private Inventory duelPlayer(Player player, Player challenged) {
        Inventory inventory = Bukkit.createInventory((InventoryHolder)player, (int)(9 * DuelCommand.cf.getInt("QUEUE.ROWS")), (String)DuelCommand.lf.getString("DUEL_COMMAND.INVENTORY_NAME").replace("%PLAYER%", challenged.getName()));
        for (Ladder ladder : Ladder.getLadders()) {
            ItemStack itemStack = Items.builder().setMaterial(ladder.getItemStack().getType()).setData(ladder.getItemStack().getDurability()).setName(DuelCommand.lf.getString("DUEL_COMMAND.ITEM_DISPLAYNAME").replace("%LADDER%", ladder.getName())).build();
            inventory.addItem(new ItemStack[]{itemStack});
        }
        return inventory;
    }
    
    public static Inventory arenaPlayer(Player player, Ladder ladder) {
    	Inventory inventory = Bukkit.createInventory((InventoryHolder)player, (int)(9 * cf.getInt("ARENA.ROWS")), (String)lf.getString("ARENA_COMMAND.INVENTORY_NAME"));
    	ItemStack r = Items.builder().setMaterial(Material.MAP).setName(ChatColor.GREEN + "Random").setLore(ChatColor.DARK_PURPLE + "Click here to choose a random arena").build();
    	inventory.addItem(r);
    	for (Arena a : ladder.getArenas()) {
    		ItemStack is = Items.builder().setMaterial(Material.EMPTY_MAP).setName(ChatColor.GREEN + a.getId()).setLore(ChatColor.DARK_PURPLE + "Click here to choose this arena").build();
    		inventory.addItem(is);
    	}
    	return inventory;
    }

    @SuppressWarnings("deprecation")
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        ArrayList<String> toReturn = new ArrayList<String>();
        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                toReturn.add(player.getName());
            }
        }
        return toReturn;
    }
}

