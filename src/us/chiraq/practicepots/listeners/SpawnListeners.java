package us.chiraq.practicepots.listeners;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.game.fight.Duel;
import us.chiraq.practicepots.profile.Profile;
import us.chiraq.practicepots.profile.ProfileManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnListeners
implements Listener {
    private Nanny main = Nanny.getInstance();
    private ProfileManager profileManager = this.main.getProfileManager();

    @SuppressWarnings("deprecation")
	@EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.profileManager.sendToSpawn(event.getPlayer());
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.profileManager.hidePlayer(event.getPlayer(), player);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player)e.getEntity();
            Profile profile = Profile.getProfile(player.getUniqueId());
            if (!profile.isInArena()) {
                e.setCancelled(true);
            }
            if (profile.getDuel() != null) {
                Duel duel = profile.getDuel();
                duel.getProfile1().getDamaged().add(player);
                duel.getProfile2().getDamaged().add(player);
            } else if (profile.getTeam() != null && profile.getTeam().getDuel() != null) {
                for (Player player1 : profile.getTeam().getDuel().getAllPlayers()) {
                    Profile profile1 = Profile.getProfile(player1.getUniqueId());
                    profile1.getDamaged().add(player);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
	@EventHandler
    public void onTabComplete(PlayerChatTabCompleteEvent e) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!e.getLastToken().isEmpty() && online.getName().startsWith(e.getLastToken())) {
                e.getTabCompletions().add(online.getName());
                continue;
            }
            if (e.getTabCompletions().contains(online.getName())) continue;
            e.getTabCompletions().add(online.getName());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Profile profile = Profile.getProfile(player.getUniqueId());
        if (profile.isInSpawn() && !player.hasPermission("staff.admin")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        final Player player = e.getPlayer();
        Profile profile = Profile.getProfile(player.getUniqueId());
        if (profile.isInSpawn()) {
            e.setCancelled(true);
            new BukkitRunnable(){

                public void run() {
                    player.updateInventory();
                }
            }.runTaskLater((Plugin)Nanny.getInstance(), 1);
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        Profile profile = Profile.getProfile(player.getUniqueId());
        if (profile.isInSpawn()) {
            e.setCancelled(true);
        } else if (!profile.getDrops().contains((Object)e.getItem())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onRespawn(final PlayerRespawnEvent e) {
        new BukkitRunnable(){

            public void run() {
                SpawnListeners.this.profileManager.sendToSpawn(e.getPlayer());
                e.getPlayer().updateInventory();
            }
        }.runTaskLater((Plugin)this.main, 20);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player)e.getWhoClicked();
        Profile profile = Profile.getProfile(player.getUniqueId());
        if (profile.isInSpawn() && !player.hasPermission("staff.admin")) {
            e.setCancelled(true);
            player.updateInventory();
        }
    }

}

