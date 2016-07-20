package us.chiraq.practicepots.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.ConfigFile;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Ladder;
import us.chiraq.practicepots.profile.Profile;
import us.chiraq.practicepots.utils.Items;

public class StatisticsCommand implements CommandExecutor {

    private Nanny main = Nanny.getInstance();
    private LangFile lf = this.main.getLangFile();
    private ConfigFile cf = this.main.getConfigFile();
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length == 1) {
				Profile checker = null;
				for (Profile p : Profile.getProfiles()) {
					if (p.getUsername().equals(args[0])) {
						checker = p;
						break;
					}
				}
				if (checker != null) {
					this.createStatsInventory(checker, ((Player)sender));
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "A player with this username does not exist!");
					return true;
				}
			} else {
				sender.sendMessage(ChatColor.RED + "/stats <playername>");
				return true;
			}
		}
		return true;
	}

	public void createStatsInventory(Profile profile, Player player) {
	      Inventory inventory = Bukkit.createInventory((InventoryHolder)player, (int)(9 * this.cf.getInt("QUEUE.ROWS")), profile.getUsername() + "'s " + (String)this.lf.getString("STATS.STATS_INVENTORY"));
	      int rwt = profile.calculateTotalFromHashMap(profile.getRankedWins());
	      int rlt = profile.calculateTotalFromHashMap(profile.getRankedLosses());

	      ArrayList<String> globalLore = new ArrayList<String>();
	      for (String string : lf.getStringList("STATS.ITEM_LORE")) {
	    	  if (string.contains("%ELO%")) globalLore.add(string.replace("%ELO%", profile.getGlobalElo() + ""));
	    	  if (string.contains("%RW%")) globalLore.add(string.replace("%RW%", rwt + ""));
	    	  if (string.contains("%RL%")) globalLore.add(string.replace("%RL%", rlt + ""));
	      }
	      String matches = this.lf.getString("STATS.TOTAL_MATCHES");
	      globalLore.add(2, matches.replace("%MATCHES%", profile.getTotalMatches() + ""));
	      ItemStack ge = Items.builder().setMaterial(Material.EYE_OF_ENDER).setName(this.lf.getString("STATS.GLOBAL_ELO_ITEM_NAME")).setLore(globalLore).build();
	      inventory.addItem(new ItemStack[]{ge});
	      ItemStack s = new ItemStack(Material.AIR);
	      for (Ladder ladder : Ladder.getLadders()) {
	          ArrayList<String> lore = new ArrayList<String>();
	          for (String string : this.lf.getStringList("STATS.ITEM_LORE")) {
	        	  if (string.contains("%ELO%")) lore.add(string.replace("%ELO%", profile.getRank().get(ladder) + ""));
	        	  if (string.contains("%RW%")) lore.add(string.replace("%RW%", profile.getRankedWins().get(ladder) + ""));
	        	  if (string.contains("%RL%")) lore.add(string.replace("%RL%", profile.getRankedLosses().get(ladder) + ""));
	          }
	          ItemStack itemStack = Items.builder().setMaterial(ladder.getItemStack().getType()).setData(ladder.getItemStack().getDurability()).setName(this.lf.getString("STATS.ITEM_NAME").replace("%LADDER%", ladder.getName())).setLore(lore).build();
	          inventory.addItem(new ItemStack[]{s, itemStack});
	      }
	      player.openInventory(inventory);
	  }
	
}
