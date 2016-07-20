package us.chiraq.practicepots.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mkremins.fanciful.FancyMessage;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.commands.DuelCommand;
import us.chiraq.practicepots.files.types.ConfigFile;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Arena;
import us.chiraq.practicepots.game.Ladder;
import us.chiraq.practicepots.game.Team;
import us.chiraq.practicepots.game.kit.Kit;
import us.chiraq.practicepots.profile.Profile;
import us.chiraq.practicepots.profile.ProfileManager;
import us.chiraq.practicepots.utils.Data;
import us.chiraq.practicepots.utils.Items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class QueueListeners
  implements Listener
{
    private Nanny main = Nanny.getInstance();
    private ProfileManager pm = this.main.getProfileManager();
    private LangFile lf = this.main.getLangFile();
    private ConfigFile cf = this.main.getConfigFile();
    private final String INVENTORY_TITLE1 = this.lf.getString("QUEUE.INVENTORY_TITLE1");
    private final String INVENTORY_TITLE2 = this.lf.getString("QUEUE.INVENTORY_TITLE2");
    private final String INVENTORY_TITLE3 = this.lf.getString("QUEUE.INVENTORY_TITLE3");
    private final String SETTINGS_INVENTORY_TITLE = this.lf.getString("SETTINGS.INVENTORY_TITLE");
  
  public QueueListeners()
  {
    new BukkitRunnable()
    {
      @SuppressWarnings("deprecation")
	public void run()
      {
        Inventory inventory;
        for (Player player : Bukkit.getOnlinePlayers()) {
        {
          Profile profile = Profile.getProfile(player.getUniqueId());
          if ((profile.isInSpawn()) && (!player.isDead()))
          {
            ((CraftPlayer)player).setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20.0F);
          }
          if (player.getOpenInventory().getType() != InventoryType.CRAFTING)
          {
            inventory = player.getOpenInventory().getTopInventory();
            if (inventory.getTitle().equalsIgnoreCase(QueueListeners.this.INVENTORY_TITLE1))
            {
              updateRankedInventory(inventory);
             }
            else if (inventory.getTitle().equalsIgnoreCase(QueueListeners.this.INVENTORY_TITLE2))
            {
              updateUnrankedInventory(inventory);
             }
            else if (inventory.getTitle().equalsIgnoreCase(QueueListeners.this.INVENTORY_TITLE3))
            {
              updatePremiumRankedInventory(inventory);
             }
           }
         }
       }
      }
    }
    
      .runTaskTimerAsynchronously(this.main, 1L, 20L);
  }
  
  @EventHandler
  public void onClose(final InventoryCloseEvent e)
  {
    if (e.getInventory().getTitle().equalsIgnoreCase(this.lf.getString("KIT_EDITOR.LOAD_NAME")))
    {
      final Player player = (Player)e.getPlayer();
      Profile profile = Profile.getProfile(player.getUniqueId());
      if ((profile.getCurrentKit() == null) && (!profile.isInSpawn())) {
        new BukkitRunnable()
        {
          public void run()
          {
            player.openInventory(e.getInventory());
          }
        }
        
          .runTaskLater(this.main, 4L);
      }
    }
  }
  
  @EventHandler
  public void onClick(InventoryClickEvent e)
  {
    Player player = (Player)e.getWhoClicked();
    Profile profile = Profile.getProfile(player.getUniqueId());
    if (profile.isInSpawn())
    {
      Team challengingTeam;
      if (e.getInventory().getTitle().equalsIgnoreCase("Duel a team"))
      {
        e.setCancelled(true);
        ItemStack itemStack = e.getCurrentItem();
        if ((itemStack != null) && (itemStack.getItemMeta() != null) && (itemStack.getItemMeta().getDisplayName() != null) && (profile.getTeam() != null))
        {
          String name = itemStack.getItemMeta().getDisplayName();
          
          name = ChatColor.stripColor(name);
          name = name.replace(" ", "");
          
          Ladder ladder = Ladder.getLadder(name);
          
          challengingTeam = profile.getTeam().getChallengingTeam();
          if ((challengingTeam != null) && (ladder != null))
          {
        	  if (player.hasPermission("practice.premium")) {
        		  profile.setSelected(ladder);
        		  DuelCommand.arenaPlayer(player, ladder);
        		  return;
        	  }
            challengingTeam.getChallenges().put(profile.getTeam(), ladder);
            for (Player challenge : challengingTeam.getMembers()) {
              if (challenge != challengingTeam.getLeader()) {
                challenge.sendMessage(this.lf.getString("TEAM.DUEL_RECEIVED").replace("%PLAYER%", player.getName()).replace("%LADDER%", ladder.getName()));
              } else {
                new FancyMessage(this.lf.getString("TEAM.DUEL_RECEIVED").replace("%PLAYER%", player.getName()).replace("%LADDER%", ladder.getName())).command("/team accept " + player.getName()).send(challenge);
              }
            }        
            profile.getTeam().sendMessage(this.lf.getString("TEAM.DUEL_SENT").replace("%PLAYER%", challengingTeam.getLeader().getName()).replace("%LADDER%", ladder.getName()));
          }
          player.closeInventory();
          return;
        }
      }
      if (e.getInventory().getTitle().equals(this.lf.getString("SETTINGS.INVENTORY_TITLE")))
      {
    	  e.setCancelled(true);
    	  ItemStack itemStack = e.getCurrentItem();
    	  if (itemStack != null) {
    		if (itemStack.getType() == Material.ENDER_PEARL)
    	  	{
    			String message;
    		  	if (profile.isShowPlayers())
    		  	{
    			  	profile.setShowPlayers(false);
    			  	Nanny.getInstance().getProfileManager().hidePlayerFromAll(player);
    			  	message = "Off";
    		  	} else {
    			  	profile.setShowPlayers(true);
    			  	Nanny.getInstance().getProfileManager().showAllPlayers(player);
    			  	message = "On";
    		  	}
    		  	player.updateInventory();
    		  	showSettingsInventory(player, profile);
    		  	player.sendMessage(this.lf.getString("SETTINGS.MESSAGES.PLAYER_VISIBILITY").replace("%VALUE%", message + ""));
    		  	return;
    	  	}
    	  	else if (itemStack.getType() == Material.PAPER)
    	  	{
    		  if (player.hasPermission("practice.premium")) {
    			  String message;
    			  if (profile.isDuelToggle()) {
    				  profile.setDuelToggle(false);
    				  message = "Off";
    			  } else {
    				  profile.setDuelToggle(true);
    				  message = "On";
    			  }
      		  	player.updateInventory();
      		  	showSettingsInventory(player, profile);
      		  	player.sendMessage(this.lf.getString("SETTINGS.MESSAGES.DUEL_TOGGLE").replace("%VALUE%", message + ""));
      		  	return;
    		  } else {
    			  player.sendMessage(this.lf.getString("DONATOR.PREMIUM.ACCESS_DENIED"));
    			  return;
    		  }
    	  	}
    	  }
    	  return;
      }
      if ((e.getInventory().getTitle().equals(this.lf.getString("STATS.STATS_INVENTORY")))) 
      {
    	  e.setCancelled(true);
    	  ItemStack itemStack = e.getCurrentItem();
    	  if ((itemStack != null) && (itemStack.getItemMeta() != null) && (itemStack.getItemMeta().getDisplayName() != null) && itemStack.getType() != Material.EYE_OF_ENDER)
    	  {
    		  String name = itemStack.getItemMeta().getDisplayName();
              name = name.replace(" ", "");
              name = ChatColor.stripColor(name);
              
              Data.showLadderLeaderboard(player, profile, Ladder.getLadder(name), -1);
    	  }
    	  if ((itemStack != null && (itemStack.getItemMeta() != null) && (itemStack.getItemMeta().getDisplayName() != null) && itemStack.getType() == Material.EYE_OF_ENDER)) {
    		  Data.showGlobalLeaderboard(player, profile, -1);
    	  }
      }
      if ((e.getInventory().getTitle().equalsIgnoreCase("Teams")) && (profile.getTeam() != null))
      {
        e.setCancelled(true);
        ItemStack itemStack = e.getCurrentItem();
        if ((itemStack != null) && (itemStack.getItemMeta() != null) && (itemStack.getItemMeta().getDisplayName() != null))
        {
          if ((itemStack.getType() == Material.ARROW) && (itemStack.getItemMeta().getDisplayName().contains("#")))
          {
            int page = Integer.valueOf(ChatColor.stripColor(itemStack.getItemMeta().getDisplayName().substring(itemStack.getItemMeta().getDisplayName().indexOf("#") + 1, itemStack.getItemMeta().getDisplayName().length()))).intValue();
            player.openInventory(this.pm.getTeamDuelInventory(player, profile.getTeam(), page));
            return;
          }
          if (itemStack.getType() == Material.BOOK)
          {
            if (profile.getTeam().getMembers().size() < 2)
            {
              player.closeInventory();
              player.sendMessage(this.lf.getString("TEAM.NOT_BIG_ENOUGH"));
              return;
            }
            Team team = Team.getTeam(ChatColor.stripColor((String)itemStack.getItemMeta().getLore().get(0)));
            if (profile.getTeam().getLeader().getName().equalsIgnoreCase(player.getName()))
            {
              profile.getTeam().setChallengingTeam(team);
              createDuelATeam(player);
              return;
            }
          }
        }
      }
    }
    if ((profile.isInArena()) && 
      (e.getInventory().getTitle().equalsIgnoreCase(this.lf.getString("KIT_EDITOR.LOAD_NAME"))))
    {
      e.setCancelled(true);
      ItemStack itemStack = e.getCurrentItem();
      if ((itemStack != null) && (itemStack.getItemMeta() != null) && (itemStack.getItemMeta().getDisplayName() != null) && ((profile.getDuel() != null) || ((profile.getTeam() != null) && (profile.getTeam().getDuel() != null))))
      {
        String name = itemStack.getItemMeta().getDisplayName();
        name = ChatColor.stripColor(name);
        Ladder ladder;
        if (profile.getDuel() != null) {
          ladder = profile.getDuel().getLadder();
        } else {
          ladder = profile.getTeam().getDuel().getLadder();
        }
        Kit kit = profile.getKit(name, ladder);
        if (kit == null)
        {
          profile.setCurrentKit(ladder.getKit());
          ladder.getKit().apply(player);
        }
        else
        {
          profile.setCurrentKit(kit);
          kit.apply(player);
        }
        player.closeInventory();
        return;
      }
    }
    if (profile.isInSpawn())
    {
      if (e.getInventory().getTitle().equalsIgnoreCase(this.lf.getString("KIT_EDITOR.INVENTORY_NAME")))
      {
        e.setCancelled(true);
        ItemStack itemStack = e.getCurrentItem();
        if ((itemStack != null) && (itemStack.getItemMeta() != null) && (itemStack.getItemMeta().getDisplayName() != null))
        {
          String name = itemStack.getItemMeta().getDisplayName();
          name = name.replace(" ", "");
          name = ChatColor.stripColor(name);
          
          Ladder ladder = Ladder.getLadder(name);
          if ((ladder != null) && (this.main.getKitEditor() != null))
          {
            profile.setInArena(true);
            profile.setInSpawn(false);
            profile.setInKitEditor(true);
            profile.setEditingLadder(ladder);
            player.teleport(this.main.getKitEditor());
            player.sendMessage(this.lf.getString("KIT_EDITOR.TELEPORTED"));
            player.getInventory().clear();
            return;
          }
        }
      }
      if (e.getInventory().getTitle().equalsIgnoreCase(this.lf.getString("DUEL_COMMAND.INVENTORY_NAME")))
      {
        e.setCancelled(true);
        ItemStack itemStack = e.getCurrentItem();
        if ((itemStack != null) && (itemStack.getItemMeta() != null) && (itemStack.getItemMeta().getDisplayName() != null))
        {
          Player challenged = profile.getDuelPlayer();
          Profile challengedProfile = Profile.getProfile(challenged.getUniqueId());
          String name = itemStack.getItemMeta().getDisplayName();
          name = name.replace(" ", "");
          name = ChatColor.stripColor(name);
          
          Ladder ladder = Ladder.getLadder(name);
          if (ladder != null && (!player.hasPermission("practice.premium")))
          {
            player.closeInventory();
            challengedProfile.getDuelRequests().put(player, ladder);
            player.sendMessage(this.lf.getString("DUEL_COMMAND.REQUEST_SENT").replace("%PLAYER%", challenged.getName()));
            new FancyMessage(this.lf.getString("DUEL_COMMAND.REQUESTED").replace("%PLAYER%", player.getName()).replace("%LADDER%", ladder.getName())).command("/duel accept " + player.getName()).send(challenged);
            return;
          } else {
        	  profile.setSelected(ladder);
        	  player.closeInventory();
        	  player.openInventory(DuelCommand.arenaPlayer(player, ladder));
        	  return;
          }
        }
      }
      if (e.getInventory().getTitle().equalsIgnoreCase(this.lf.getString("ARENA_COMMAND.INVENTORY_NAME")))
      {
    	  e.setCancelled(true);
    	  ItemStack is = e.getCurrentItem();
    	  if (is != null && is.getItemMeta() != null && is.getItemMeta().getDisplayName() != null)
    	  {
    		  if (!(profile.getTeam() != null)) {
    			  Player challenged = profile.getDuelPlayer();
    			  Profile challengedProfile = Profile.getProfile(challenged.getUniqueId());
    			  String name = is.getItemMeta().getDisplayName();
    			  name = ChatColor.stripColor(name);
              
    			  Ladder l = profile.getSelected();
              
    			  Arena a;
    			  if (name.equalsIgnoreCase("random")) {
    				  a = l.getArenas().get(new Random().nextInt(l.getArenas().size()));
    			  } else {
    				  a = Arena.getArena(name);
    			  }
              
    			  profile.setArena(a);
              
    			  player.closeInventory();
              
    			  challengedProfile.getDuelRequests().put(player, l);
    			  player.sendMessage(this.lf.getString("DUEL_COMMAND.REQUEST_SENT").replace("%PLAYER%", challenged.getName()));
    			  new FancyMessage(this.lf.getString("DUEL_COMMAND.DONATOR_REQUESTED").replace("%PLAYER%", player.getName()).replace("%LADDER%", l.getName()).replace("%ARENA%", name)).command("/duel accept " + player.getName()).send(challenged);
    			  return;
    		  } else {
    			  Team challengingTeam;
    			  challengingTeam = profile.getTeam().getChallengingTeam();
    			  
    			  String name = is.getItemMeta().getDisplayName();
    			  name = ChatColor.stripColor(name);
    			  
    			  Ladder l = profile.getSelected();
    			  
    			  Arena a;
    			  if (name.equalsIgnoreCase("random")) {
    				  a = l.getArenas().get(new Random().nextInt(l.getArenas().size()));
    			  } else {
    				  a = Arena.getArena(name);
    			  }
              
    			  profile.setArena(a);
    			  
    			  challengingTeam.getChallenges().put(profile.getTeam(), l);
    	            for (Player challenge : challengingTeam.getMembers()) {
    	              if (challenge != challengingTeam.getLeader()) {
    	                challenge.sendMessage(this.lf.getString("TEAM.DUEL_RECEIVED").replace("%PLAYER%", player.getName()).replace("%LADDER%", l.getName()));
    	              } else {
    	                new FancyMessage(this.lf.getString("TEAM.DONATOR_DUEL_RECEIVED").replace("%PLAYER%", player.getName()).replace("%LADDER%", l.getName())).command("/team accept " + player.getName()).send(challenge);
    	              }
    	            }
    	            profile.getTeam().sendMessage(this.lf.getString("TEAM.DUEL_SENT").replace("%PLAYER%", challengingTeam.getLeader().getName()).replace("%LADDER%", l.getName()));
    	          }
    	          player.closeInventory();
    		  }
    		  return;
    	  }
      }
      if ((e.getInventory().getTitle().equalsIgnoreCase(this.INVENTORY_TITLE1)) && (e.getCurrentItem() != null))
      {
        e.setCancelled(true);
        ItemStack itemStack = e.getCurrentItem();
        if ((itemStack != null) && (itemStack.getItemMeta() != null) && (itemStack.getItemMeta().getDisplayName() != null))
        {
          String name = itemStack.getItemMeta().getDisplayName();
          name = name.substring(0, name.indexOf("Queue")).replace(" ", "");
          name = ChatColor.stripColor(name);
          
          Ladder ladder = Ladder.getLadder(name);
          if (ladder != null) {
            if (e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT)
            {
              player.closeInventory();
              player.getInventory().clear();
              profile.putInQueue(ladder, 0);
              this.main.getProfileManager().giveQuitItem(player);
              return;
            }
          }
        }
      }
      if ((e.getInventory().getTitle().equalsIgnoreCase(this.INVENTORY_TITLE2)) && (e.getCurrentItem() != null))
      {
        e.setCancelled(true);
        ItemStack itemStack = e.getCurrentItem();
        if ((itemStack != null) && (itemStack.getItemMeta() != null) && (itemStack.getItemMeta().getDisplayName() != null))
        {
          String name = itemStack.getItemMeta().getDisplayName();
          name = name.substring(0, name.indexOf("Queue")).replace(" ", "");
          name = ChatColor.stripColor(name);
          
          Ladder ladder = Ladder.getLadder(name);
          if (ladder != null) {
            if (e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT)
            {
              player.closeInventory();
              player.getInventory().clear();
              profile.putInQueue(ladder, 1);
              this.main.getProfileManager().giveQuitItem(player);
              return;
            }
          }
        }
      }
      if ((e.getInventory().getTitle().equalsIgnoreCase(this.INVENTORY_TITLE3)) && (e.getCurrentItem() != null))
      {
    	  
        e.setCancelled(true);
        ItemStack itemStack = e.getCurrentItem();
        if ((itemStack != null) && (itemStack.getItemMeta() != null) && (itemStack.getItemMeta().getDisplayName() != null))
        {
          String name = itemStack.getItemMeta().getDisplayName();
          name = name.substring(0, name.indexOf("Queue")).replace(" ", "");
          name = ChatColor.stripColor(name);
          
          Ladder ladder = Ladder.getLadder(name);
          if (ladder != null) {
            if (e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT)
            {
              player.closeInventory();
              player.getInventory().clear();
              profile.putInQueue(ladder, 2);
              this.main.getProfileManager().giveQuitItem(player);
              return;
            }
          }
        }
      }
    }
  
  @EventHandler
  public void onInteract(PlayerInteractEvent e) {
      Inventory inventory;
      Player player = e.getPlayer();
      Profile profile = Profile.getProfile(player.getUniqueId());
      if (e.getAction().name().contains("RIGHT") && e.getItem() != null && e.getItem().getType() == Material.NETHER_STAR && profile.isInSpawn() && profile.getTeam() != null) {
          e.setCancelled(true);
          Team team = profile.getTeam();
          player.sendMessage(this.lf.getString("TEAM.LIST"));
          for (Player p : team.getMembers()) {
        	  if (team.getLeader().equals(p)) {
        		  player.sendMessage(ChatColor.BOLD.toString() + ChatColor.YELLOW + p.getName());
        	  } else player.sendMessage(ChatColor.YELLOW + p.getName());
          }
          return;
      }
      if (e.getAction().name().contains("RIGHT") && e.getItem() != null && e.getItem().getType() == Material.NAME_TAG) {
          e.setCancelled(true);
          if (profile.isInSpectator()) {
        	  player.sendMessage(this.lf.getString("SPECTATOR.IN_SPECTATOR_MESSAGE"));
        	  return;
          }
          if (profile.isInSpawn() && profile.getTeam() == null) {
              profile.setTeam(new Team(player));
              return;
          }
      }
      if (e.getAction().name().contains("RIGHT") && e.getItem() != null && e.getItem().getType() == Material.WATCH) {
    	  e.setCancelled(true);
          if (profile.isInSpectator()) {
        	  player.sendMessage(this.lf.getString("SPECTATOR.IN_SPECTATOR_MESSAGE"));
        	  return;
          }
    	  if (profile.isInSpawn()) {
    		  showSettingsInventory(player, profile);
    		  return;
    	  }
      }
      if (e.getAction().name().contains("RIGHT") && e.getItem() != null && e.getItem().getType() == Material.SKULL_ITEM) {
          e.setCancelled(true);
          if (profile.isInSpectator()) {
        	  return;
          }
          if (profile.isInSpawn()) {
              inventory = Bukkit.createInventory((InventoryHolder)player, (int)(9 * this.cf.getInt("QUEUE.ROWS")), (String)this.lf.getString("STATS.STATS_INVENTORY"));
              int rwt = profile.calculateTotalFromHashMap(profile.getRankedWins());
              int rlt = profile.calculateTotalFromHashMap(profile.getRankedLosses());

              ArrayList<String> globalLore = new ArrayList<String>();
              for (String string : this.lf.getStringList("STATS.ITEM_LORE")) {
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
              return;
          }
      }
      if (e.getAction().name().contains("RIGHT") && e.getItem() != null && e.getItem().getType() == Material.PAPER && profile.isInSpectator()) {
    	  Profile dueler = pm.getRandomDuelingProfile(player);
    	  if (dueler == null) return;
    	  pm.spectatePlayer(Bukkit.getPlayer(dueler.getUuid()), player, dueler, profile);
    	  return;
      }
      if (e.getAction().name().contains("RIGHT") && e.getItem() != null && e.getItem().getType() == Material.BOOK && profile.isInSpawn()) {
          e.setCancelled(true);
          if (profile.isInSpectator()) {
        	  player.sendMessage(this.lf.getString("SPECTATOR.IN_SPECTATOR_MESSAGE"));
        	  return;
          }
          if (profile.getTeam() != null) {
              player.openInventory(this.pm.getTeamDuelInventory(player, profile.getTeam(), 1));
              return;
          }
          inventory = Bukkit.createInventory((InventoryHolder)player, (int)(9 * this.cf.getInt("QUEUE.ROWS")), (String)this.lf.getString("KIT_EDITOR.INVENTORY_NAME"));
          for (Ladder ladder : Ladder.getLadders()) {
              ItemStack itemStack = Items.builder().setMaterial(ladder.getItemStack().getType()).setData(ladder.getItemStack().getDurability()).setName(this.lf.getString("KIT_EDITOR.ITEM_DISPLAYNAME").replace("%LADDER%", ladder.getName())).build();
              inventory.addItem(new ItemStack[]{itemStack});
          }
          player.openInventory(inventory);
          return;
      }
      if ((e.getAction().name().contains("RIGHT")) && (e.getItem() != null) && (e.getItem().getType() == Material.DIAMOND_SWORD) && 
    	      (profile.isInSpawn()))
    	    {
    	      e.setCancelled(true);
    	      if (!profile.isRankedUnlocked()) {
    	    	  if (profile.calculateTotalFromHashMap(profile.getRankedWins()) >= 5) {
    	    		  profile.setRankedUnlocked(true);
    	    	  }
    	      }
              if (profile.isInSpectator()) {
            	  player.sendMessage(this.lf.getString("SPECTATOR.IN_SPECTATOR_MESSAGE"));
            	  return;
              }
              if (profile.isQueueCooldown()) {
            	  player.sendMessage(ChatColor.RED + "Wait two seconds before queueing!");
              }
              Inventory i = Bukkit.createInventory(player, 9 * this.cf.getInt("QUEUE.ROWS"), this.INVENTORY_TITLE1);
    	      updateRankedInventory(i);
    	      player.openInventory(i);
    	      return;
    	    }
      if ((e.getAction().name().contains("RIGHT")) && (e.getItem() != null) && (e.getItem().getType() == Material.IRON_SWORD) && (profile.isInSpawn()))
      {
    	  e.setCancelled(true);
          if (profile.isInSpectator()) {
        	  player.sendMessage(this.lf.getString("SPECTATOR.IN_SPECTATOR_MESSAGE"));
        	  return;
          }
          if (profile.isQueueCooldown()) {
        	  player.sendMessage(ChatColor.RED + "Wait two seconds before queueing!");
          }
          Inventory i = Bukkit.createInventory(player, 9 * this.cf.getInt("QUEUE.ROWS"), this.INVENTORY_TITLE2);
          updateUnrankedInventory(i);
    	  player.openInventory(i);
    	  return;
      }
      if ((e.getAction().name().contains("RIGHT")) && (e.getItem() != null) && (e.getItem().getType() == Material.GOLD_SWORD) && (profile.isInSpawn()))
      {
    	  e.setCancelled(true);
    	  if (!player.hasPermission("practice.premium"))
    	  {
    		  player.sendMessage(this.lf.getString("DONATOR.PREMIUM.ACCESS_DENIED"));
    		  return;
    	  }
          if (profile.isInSpectator()) {
        	  player.sendMessage(this.lf.getString("SPECTATOR.IN_SPECTATOR_MESSAGE"));
        	  return;
          }
          if (profile.isQueueCooldown()) {
        	  player.sendMessage(ChatColor.RED + "Wait two seconds before queueing!");
          }
          Inventory i = Bukkit.createInventory(player, 9 * this.cf.getInt("QUEUE.ROWS"), this.INVENTORY_TITLE3);
          updatePremiumRankedInventory(i);
    	  player.openInventory(i);
    	  return;
      }
    	      else if (e.getItem() != null) {
    	    	  if (e.getItem().getType() != Material.AIR) {
    	    		  if (e.getItem().getDurability() == 1 && (e.getItem().getType() == Material.INK_SACK))
    	      			{
    	    			  if (profile.isInSpectator()) {
    	    				  e.setCancelled(true);
    	    				  player.setGameMode(GameMode.SURVIVAL);
    	    				  Profile.getProfile(profile.getSpectatingPlayer().getUniqueId()).removeSpectator(player);
    	    				  profile.setSpectating(null);
    	    				  profile.setInSpectator(false);
    	    				  pm.hidePlayerFromAll(player);
    	    				  pm.sendToSpawn(player);
    	    				  return;
    	    			  }
    	    			  if (profile.getTeam() != null)
    	    			  {
    	    				  Team team = profile.getTeam();
    	    				  if (team.getLeader().getName().equals(player.getName()))
    	    				  {
    	    					  team.sendMessage(this.lf.getString("TEAM.DELETED"));
    	    					  team.delete();
    	    				  }
    	    				  else
    	    				  {
    	    					  team.sendMessage(this.lf.getString("TEAM.LEFT").replace("%PLAYER%", player.getName()));
    	    					  team.removePlayer(player);
    	    				  }
    	    				  return;
    	    			  }
    	    			  if (profile.getQueue() != null)
    	    			  {
    	    				  profile.getQueue().cancel();
    	    				  profile.getSearchingLadder().getUnrankedQueue().remove(player);
    	    				  profile.getSearchingLadder().getRankedQueue().remove(player);
    	    				  profile.getSearchingLadder().getPremiumRankedQueue().remove(player);
    	    				  profile.setQueue(null);
    	    				  player.sendMessage(this.lf.getString("QUEUE.CANCEL"));
    	    			  }
    	    			  this.main.getProfileManager().giveSpawnItems(player);
    	    			  return;
    	      			}
    	    	  }
    	      }
  }
  
  @SuppressWarnings("deprecation")
@EventHandler
  public void onQuit(PlayerQuitEvent e)
  {
    for (Player player : Bukkit.getOnlinePlayers())
    {
      player.hidePlayer(e.getPlayer());
      EntityPlayer nmsFrom = ((CraftPlayer)player).getHandle();
      EntityPlayer nmsHiding = ((CraftPlayer)e.getPlayer()).getHandle();
      PacketPlayOutPlayerInfo packet = PacketPlayOutPlayerInfo.removePlayer(nmsHiding);
      nmsFrom.playerConnection.sendPacket(packet);
    }
  }
  
  public void showSettingsInventory(Player player, Profile profile) {
	  Inventory inventory = Bukkit.createInventory((InventoryHolder)player, (int)(9 * this.cf.getInt("SETTINGS.ROWS")), SETTINGS_INVENTORY_TITLE);
	  List<String> temp = new ArrayList<String>();
	  String message;
	  if (profile.isShowPlayers()) {
		  message = "On";
	  } else {
		  message = "Off";
	  }
	  for (String s : this.lf.getStringList("SETTINGS.ENDERPEARL.LORE")) {
		  temp.add(s.replace("%VALUE%", message + ""));
	  }
	  ItemStack pearl = Items.builder().setMaterial(Material.ENDER_PEARL).setName(this.lf.getString("SETTINGS.ENDERPEARL.NAME")).setLore(temp).build();
	  temp.clear();
	  if (profile.isDuelToggle()) {
		  message = "On";
	  } else {
		  message = "Off";
	  }
	  for (String s : this.lf.getStringList("SETTINGS.PAPER.LORE")) {
		  temp.add(s.replace("%VALUE%", message + ""));
	  }
	  ItemStack paper = Items.builder().setMaterial(Material.PAPER).setName(this.lf.getString("SETTINGS.PAPER.NAME")).setLore(temp).build();
	  inventory.addItem(new ItemStack[]{pearl, paper});
	  player.openInventory(inventory);
  }
  
  public void createDuelATeam(Player player) {
      Inventory inventory = Bukkit.createInventory(player, 9 * this.cf.getInt("QUEUE.ROWS"), "Duel a team");
      for (Ladder ladder : Ladder.getLadders())
      {
        ItemStack itemToSet = Items.builder().setMaterial(ladder.getItemStack().getType()).setData(ladder.getItemStack().getDurability()).setName(this.lf.getString("KIT_EDITOR.ITEM_DISPLAYNAME").replace("%LADDER%", ladder.getName())).build();
        inventory.addItem(new ItemStack[] { itemToSet });
      }
      player.openInventory(inventory);
  }
  
  public void updateRankedInventory(Inventory inventory) {
	  inventory.clear();
      for (Ladder ladder : Ladder.getLadders())
      {
        int size = ladder.getRankedQueue().size();
        List<String> lore = new ArrayList<String>();
        for (String string : QueueListeners.this.lf.getStringList("QUEUE.ITEM.LORE"))
        {
          lore.add(string.replace("%SIZE%", size + "").replace("%LADDER%", ladder.getName()).replace("%MATCHES%", ladder.getCurrentRankedMatches() + ""));
        }
        ItemStack itemStack = Items.builder().setMaterial(ladder.getItemStack().getType()).setData(ladder.getItemStack().getDurability()).setName(QueueListeners.this.lf.getString("QUEUE.ITEM.DISPLAYNAME").replace("%SIZE%", size + "").replace("%LADDER%", ladder.getName())).setLore(lore).build();
        inventory.addItem(new ItemStack[] { itemStack });
       }
  }
  
  public void updateUnrankedInventory(Inventory inventory) {
      inventory.clear();
      for (Ladder ladder : Ladder.getLadders())
      {
        int size = ladder.getUnrankedQueue().size();
        List<String> lore = new ArrayList<String>();
        for (String string : QueueListeners.this.lf.getStringList("QUEUE.ITEM.LORE"))
        {
          lore.add(string.replace("%SIZE%", size + "").replace("%LADDER%", ladder.getName()).replace("%MATCHES%", ladder.getCurrentUnRankedMatches() + ""));
        }
        ItemStack itemStack = Items.builder().setMaterial(ladder.getItemStack().getType()).setData(ladder.getItemStack().getDurability()).setName(QueueListeners.this.lf.getString("QUEUE.ITEM.DISPLAYNAME").replace("%SIZE%", size + "").replace("%LADDER%", ladder.getName())).setLore(lore).build();
        inventory.addItem(new ItemStack[] { itemStack });
       }
  }
  
  public void updatePremiumRankedInventory(Inventory inventory) {
      inventory.clear();
      for (Ladder ladder : Ladder.getLadders())
      {
        int size = ladder.getPremiumRankedQueue().size();
        List<String> lore = new ArrayList<String>();
        for (String string : QueueListeners.this.lf.getStringList("QUEUE.ITEM.LORE"))
        {
          lore.add(string.replace("%SIZE%", size + "").replace("%LADDER%", ladder.getName()).replace("%MATCHES%", ladder.getCurrentPremiumRankedMatches() + ""));
        }
        ItemStack itemStack = Items.builder().setMaterial(ladder.getItemStack().getType()).setData(ladder.getItemStack().getDurability()).setName(QueueListeners.this.lf.getString("QUEUE.ITEM.DISPLAYNAME").replace("%SIZE%", size + "").replace("%LADDER%", ladder.getName())).setLore(lore).build();
        inventory.addItem(new ItemStack[] { itemStack });
       }
  }
  
}
