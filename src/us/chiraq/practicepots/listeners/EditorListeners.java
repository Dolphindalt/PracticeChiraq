package us.chiraq.practicepots.listeners;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Ladder;
import us.chiraq.practicepots.game.kit.Kit;
import us.chiraq.practicepots.profile.Profile;
import us.chiraq.practicepots.profile.ProfileManager;
import us.chiraq.practicepots.utils.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EditorListeners
implements Listener {
    private Nanny main = Nanny.getInstance();
    private LangFile lf = this.main.getLangFile();
    private ProfileManager pm = this.main.getProfileManager();
    private Map<Player, Kit> settingName = new HashMap<Player, Kit>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player)e.getWhoClicked();
        Profile profile = Profile.getProfile(player.getUniqueId());
        if (profile.isInKitEditor() && e.getInventory().getName().contains("Manage") && e.getInventory().getName().contains("Kits") && e.getCurrentItem() != null) {
            e.setCancelled(true);
            Ladder ladder = profile.getEditingLadder();
            ItemStack item = e.getCurrentItem();
            if (item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null) {
                Kit kit;
                String name = item.getItemMeta().getDisplayName();
                if (name.contains("Save")) {
                    Kit kit2 = profile.getKit(name = ChatColor.stripColor((String)name.substring(name.indexOf((Object)ChatColor.GOLD + ""), name.length())), ladder);
                    if (kit2 != null) {
                        kit2 = new Kit(ladder, player.getInventory().getArmorContents(), player.getInventory().getContents());
                        kit2.setName(name);
                        player.closeInventory();
                        player.sendMessage(this.lf.getString("KIT_EDITOR.SAVED").replace("%KIT%", name));
                        return;
                    }
                    if (name.contains("Custom") && name.contains("#")) {
                        name = name.substring(0, name.indexOf("#"));
                        name = name + "#" + (profile.getKits(ladder).size() + 1);
                    }
                    Kit newKit = new Kit(ladder, player.getInventory().getArmorContents(), player.getInventory().getContents());
                    newKit.setName(name);
                    profile.getKits().add(newKit);
                    player.closeInventory();
                    player.sendMessage(this.lf.getString("KIT_EDITOR.SAVED").replace("%KIT%", name));
                    return;
                }
                if (name.contains("Load")) {
                    Kit kit3 = profile.getKit(name = ChatColor.stripColor((String)name.substring(name.indexOf((Object)ChatColor.GOLD + ""), name.length())), ladder);
                    if (kit3 != null) {
                        kit3.apply(player);
                        player.closeInventory();
                        player.sendMessage(this.lf.getString("KIT_EDITOR.LOADED").replace("%KIT%", name));
                        return;
                    }
                } else if (name.contains("Delete")) {
                    Kit kit4 = profile.getKit(name = ChatColor.stripColor((String)name.substring(name.indexOf((Object)ChatColor.GOLD + ""), name.length())), ladder);
                    if (kit4 != null) {
                        profile.getKits().remove(kit4);
                        player.closeInventory();
                        player.sendMessage(this.lf.getString("KIT_EDITOR.DELETED").replace("%KIT%", name));
                        return;
                    }
                } else if (name.contains("Rename") && (kit = profile.getKit(name = ChatColor.stripColor((String)name.substring(name.indexOf((Object)ChatColor.GOLD + ""), name.length())), ladder)) != null) {
                    player.closeInventory();
                    this.settingName.put(player, kit);
                    player.sendMessage(this.lf.getString("KIT_EDITOR.TALK").replace("%KIT%", name));
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onTalk(AsyncPlayerChatEvent e) {
        if (this.settingName.containsKey((Object)e.getPlayer())) {
            e.getPlayer().sendMessage(this.lf.getString("KIT_EDITOR.RENAMED").replace("%KIT%", this.settingName.get((Object)e.getPlayer()).getName()));
            this.settingName.get((Object)e.getPlayer()).setName(e.getMessage());
            this.settingName.remove((Object)e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockClick(PlayerInteractEvent e)
    {
      Player player = e.getPlayer();
      Profile profile = Profile.getProfile(player.getUniqueId());
      if ((profile.isInKitEditor()) && 
        (e.getAction().name().contains("BLOCK")))
      {
        Items.ItemStackBuilder chestTemplate;
        List<Kit> kits;
        int i;
        if (e.getClickedBlock().getType() == Material.ANVIL)
        {
          Ladder ladder = profile.getEditingLadder();
          e.setCancelled(true);
          Inventory inventory = Bukkit.createInventory(player, 36, "Manage " + ladder.getName() + " Kits");
          chestTemplate = Items.builder().setMaterial(Material.CHEST);
          kits = profile.getKits(ladder);
          for (i = 0; i < 5; i++) {
            if (kits.size() > i)
            {
              Kit kit = (Kit)kits.get(i);
              ItemStack saveKit = chestTemplate.setName(ChatColor.YELLOW + "Save kit: " + ChatColor.GOLD + kit.getName()).build();
              
              ItemStack loadKit = Items.builder().setMaterial(Material.ENCHANTED_BOOK).setName(ChatColor.YELLOW + "Load kit: " + ChatColor.GOLD + kit.getName()).build();
              
              ItemStack renameKit = Items.builder().setMaterial(Material.NAME_TAG).setName(ChatColor.YELLOW + "Rename kit: " + ChatColor.GOLD + kit.getName()).build();
              
              ItemStack deleteKit = Items.builder().setMaterial(Material.FIRE).setName(ChatColor.YELLOW + "Delete kit: " + ChatColor.GOLD + kit.getName()).build();
              inventory.setItem(i * 2, saveKit);
              inventory.setItem(i * 2 + 9, loadKit);
              inventory.setItem(i * 2 + 18, renameKit);
              inventory.setItem(i * 2 + 27, deleteKit);
            }
            else
            {
              ItemStack saveKit = chestTemplate.setName(ChatColor.YELLOW + "Save kit: " + ChatColor.GOLD + "Custom " + ladder.getName() + " Kit #" + (i + 1)).build();
              inventory.setItem(i * 2, saveKit);
            }
          }
          player.openInventory(inventory);
        }
        if ((e.getClickedBlock().getState() instanceof Chest))
        {
          e.setCancelled(true);
          Inventory inventory = Bukkit.createInventory(player, 45, "Chest");
          Kit kit = profile.getEditingLadder().getKit();
          if (kit != null)
          {
            for (ItemStack itemStack : kit.getArmor()) {
              if (itemStack != null) {
                inventory.addItem(new ItemStack[] { itemStack });
              }
            }
            for (ItemStack itemStack : kit.getInventory()) {
              if (itemStack != null) {
                inventory.addItem(new ItemStack[] { itemStack });
              }
            }
          }
          player.openInventory(inventory);
        }
        if ((e.getClickedBlock().getState() instanceof Sign))
        {
          Sign sign = (Sign)e.getClickedBlock().getState();
          if (sign.getLine(1).contains(this.lf.getString("KIT_EDITOR.SIGN_RETURN")))
          {
            profile.setEditingLadder(null);
            profile.setInKitEditor(false);
            profile.setInSpawn(true);
            
            player.sendMessage(this.lf.getString("KIT_EDITOR.RETURNED"));
            this.pm.sendToSpawn(player);
          }
        }
      }
    }
  }


