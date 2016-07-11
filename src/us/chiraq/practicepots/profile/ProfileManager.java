package us.chiraq.practicepots.profile;

import com.comphenix.protocol.ProtocolManager;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.ConfigFile;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Team;
import us.chiraq.practicepots.utils.Items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class ProfileManager
{
  private Nanny main = Nanny.getInstance();
  @SuppressWarnings("unused")
private ProtocolManager protocolManager = this.main.getProtocolManager();
  private ConfigFile cf = this.main.getConfigFile();
  private LangFile lf = this.main.getLangFile();
  
  public Inventory getTeamDuelInventory(Player player, Team team, int page)
  {
    Inventory inventory = Bukkit.createInventory(player, 54, "Teams");
    int goal = Team.getTeams().size() % (page * 54);
    if (Team.getTeams().size() > 54 * page)
    {
      ItemStack nextPage = Items.builder().setMaterial(Material.ARROW).setName(this.lf.getString("TEAM.GUI.NEXT_PAGE").replace("%PAGE%", page + 1 + "")).build();
      inventory.setItem(53, nextPage);
      goal = 54 * page - 52;
    }
    List<String> lore = new ArrayList<String>();
    for (Player teamPlayer : team.getMembers()) {
      lore.add(this.lf.getString("TEAM.GUI.ITEM_LORE").replace("%PLAYER%", teamPlayer.getName()));
    }
    inventory.addItem(new ItemStack[] {Items.builder()
      .setMaterial(Material.SKULL_ITEM)
      .setData((short) 3)
      .setName(this.lf.getString("TEAM.GUI.ITEM_NAME").replace("%PLAYER%", team.getLeader().getName()))
      .setLore(lore)
      .build() });
    for (int i = Team.getTeams().size() - goal; i < goal; i++)
    {
      Team teamToAdd = (Team)Team.getTeams().get(i);
      if ((teamToAdd != team) && (teamToAdd.getMembers().size() > 1))
      {
        lore = new ArrayList<String>();
        for (Player teamToAddPlayer : teamToAdd.getMembers()) {
          lore.add(this.lf.getString("TEAM.GUI.ITEM_LORE").replace("%PLAYER%", teamToAddPlayer.getName()));
        }
        Items.ItemStackBuilder builder = Items.builder().setAmount(teamToAdd.getMembers().size()).setName(this.lf.getString("TEAM.GUI.ITEM_NAME").replace("%PLAYER%", teamToAdd.getLeader().getName())).setLore(lore);
        if (teamToAdd.isInFight()) {
          builder.setMaterial(Material.PAPER);
        } else {
          builder.setMaterial(Material.BOOK);
        }
        inventory.addItem(new ItemStack[] { builder.build() });
      }
    }
    return inventory;
  }
  
  @SuppressWarnings("deprecation")
public void sendToSpawn(Player player) {
      player.teleport(player.getWorld().getSpawnLocation());
      Profile profile = Profile.getProfile(player.getUniqueId()) == null ? new Profile(player.getUniqueId()) : Profile.getProfile(player.getUniqueId());
      profile.setCurrentKit(null);
      profile.setDuel(null);
      profile.setInSpawn(true);
      for (PotionEffect potionEffect : player.getActivePotionEffects()) {
          player.removePotionEffect(potionEffect.getType());
      }
      if (!profile.isShowPlayers()) {
    	  for (Player online : Bukkit.getOnlinePlayers()) {
    	  	if (online == player) continue;
          	this.hidePlayer(online, player);
      	}
      }
      player.setFoodLevel(20);
      player.setHealth(20);
      player.setFireTicks(0);
      if (profile.getTeam() != null) {
          this.giveTeamItems(player);
      } else {
          this.giveSpawnItems(player);
      }
  }
  
  public void giveTeamItems(Player player)
  {
    ItemStack book = Items.builder().setMaterial(Material.BOOK).setName(this.cf.getString("HOTBAR_ITEMS.TEAM.BOOK.DISPLAYNAME")).setLore(this.cf.getStringList("HOTBAR_ITEMS.TEAM.BOOK.LORE")).build();
    
    ItemStack star = Items.builder().setMaterial(Material.NETHER_STAR).setName(this.cf.getString("HOTBAR_ITEMS.TEAM.STAR.DISPLAYNAME")).setLore(this.cf.getStringList("HOTBAR_ITEMS.TEAM.STAR.LORE")).build();
    
    ItemStack redDye = Items.builder().setMaterial(Material.INK_SACK).setData((short)1).setName(this.cf.getString("HOTBAR_ITEMS.TEAM.RED_DYE.DISPLAYNAME")).setLore(this.cf.getStringList("HOTBAR_ITEMS.TEAM.RED_DYE.LORE")).build();
    
    player.getInventory().clear();
    player.getInventory().setArmorContents(null);
    
    player.getInventory().setItem(0, redDye);
    player.getInventory().setItem(4, book);
    player.getInventory().setItem(8, star);
    
    player.updateInventory();
  }
  
  public void giveSpawnItems(Player player)
  {
    if (this.cf.getBoolean("HOTBAR_ITEMS.ENABLED"))
    {
      ItemStack book = Items.builder().setMaterial(Material.BOOK).setName(this.cf.getString("HOTBAR_ITEMS.BOOK.DISPLAYNAME")).setLore(this.cf.getStringList("HOTBAR_ITEMS.BOOK.LORE")).build();
      
      ItemStack skull = Items.builder().setMaterial(Material.SKULL_ITEM).setData((short)3).setName(this.cf.getString("HOTBAR_ITEMS.SKULL.DISPLAYNAME")).setLore(this.cf.getStringList("HOTBAR_ITEMS.SKULL.LORE")).build();
      
      ItemStack leash = Items.builder().setMaterial(Material.NAME_TAG).setName(this.cf.getString("HOTBAR_ITEMS.LEASH.DISPLAYNAME")).setLore(this.cf.getStringList("HOTBAR_ITEMS.LEASH.LORE")).build();
      
      ItemStack watch = Items.builder().setMaterial(Material.WATCH).setName(this.cf.getString("HOTBAR_ITEMS.WATCH.DISPLAYNAME")).setLore(this.cf.getStringList("HOTBAR_ITEMS.WATCH.LORE")).build();
      
      ItemStack blueDye = Items.builder().setMaterial(Material.DIAMOND_SWORD).setName(this.cf.getString("HOTBAR_ITEMS.BLUE_DYE.DISPLAYNAME")).setLore(this.cf.getStringList("HOTBAR_ITEMS.BLUE_DYE.LORE")).build();
      ItemStack goldDye = Items.builder().setMaterial(Material.GOLD_SWORD).setName(this.cf.getString("HOTBAR_ITEMS.GOLD_DYE.DISPLAYNAME")).setLore(this.cf.getStringList("")).setLore(this.cf.getStringList("HOTBAR_ITEMS.GOLD_DYE.LORE")).build();
      ItemStack ironDye = Items.builder().setMaterial(Material.IRON_SWORD).setName(this.cf.getString("HOTBAR_ITEMS.IRON_DYE.DISPLAYNAME")).setLore(this.cf.getStringList("")).setLore(this.cf.getStringList("HOTBAR_ITEMS.IRON_DYE.LORE")).build();
      
      player.getInventory().clear();
      player.getInventory().setArmorContents(null);
      
      player.getInventory().setItem(0, blueDye);
      player.getInventory().setItem(1, goldDye);
      player.getInventory().setItem(2, ironDye);
      player.getInventory().setItem(4, watch);
      player.getInventory().setItem(6, leash);
      player.getInventory().setItem(7, skull);
      player.getInventory().setItem(8, book);
      
      player.updateInventory();
    }
  }
  
  public void giveQuitItem(Player player)
  {
    ItemStack red = Items.builder().setMaterial(Material.INK_SACK).setData((short)1).setName(this.cf.getString("HOTBAR_ITEMS.RED_DYE.DISPLAYNAME")).setLore(this.cf.getStringList("HOTBAR_ITEMS.RED_DYE.LORE")).build();
    player.getInventory().setItem(0, red);
  }
  
  @SuppressWarnings("deprecation")
public void setupPlayers()
  {
    for (Player player : Bukkit.getOnlinePlayers()) {
      sendToSpawn(player);
    }
  }
  
  public void hidePlayer(Player hiding, Player from)
  {
    from.hidePlayer(hiding);
    EntityPlayer nmsFrom = ((CraftPlayer)from).getHandle();
    EntityPlayer nmsHiding = ((CraftPlayer)hiding).getHandle();
    PacketPlayOutPlayerInfo packet = PacketPlayOutPlayerInfo.addPlayer(nmsHiding);
    nmsFrom.playerConnection.sendPacket(packet);
  }
  
  @SuppressWarnings("deprecation")
public void updateStaffView()
  {
    for (Player staff : Bukkit.getOnlinePlayers()) {
      if (staff.hasPermission("staff.mod"))
      {
        Profile staffProfile = Profile.getProfile(staff.getUniqueId());
        for (Player player : Bukkit.getOnlinePlayers())
        {
          Profile playerProfile = Profile.getProfile(player.getUniqueId());
          if ((staffProfile.isInSpawn()) && (!playerProfile.isInSpawn())) {
            staff.showPlayer(player);
          }
        }
      }
    }
  }
  
  @SuppressWarnings("deprecation")
public void hidePlayerFromAll(Player player) {
	  for (Player online : Bukkit.getOnlinePlayers()) {
  	  	if (online == player) continue;
        	this.hidePlayer(online, player);
    	}
  }
  
@SuppressWarnings("deprecation")
public void showAllPlayers(Player player) {
	for (Player online : Bukkit.getOnlinePlayers()) {
		if (online == player) continue;
		player.showPlayer(online);
	}
}

public void showPlayer(Player hiding, Player from) {
	from.showPlayer(hiding);
	EntityPlayer nmsFrom = ((CraftPlayer)from).getHandle();
	EntityPlayer nmsHiding = ((CraftPlayer)hiding).getHandle();
	PacketPlayOutPlayerInfo packet = PacketPlayOutPlayerInfo.removePlayer(nmsHiding);
	nmsFrom.playerConnection.sendPacket(packet);
}
  
}
