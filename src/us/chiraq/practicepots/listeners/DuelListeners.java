package us.chiraq.practicepots.listeners;

import java.util.Arrays;

import mkremins.fanciful.FancyMessage;
import net.minecraft.server.v1_7_R4.EnumClientCommand;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInClientCommand;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityDestroy;
import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.ConfigFile;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Team;
import us.chiraq.practicepots.game.fight.Duel;
import us.chiraq.practicepots.game.fight.TeamDuel;
import us.chiraq.practicepots.profile.Profile;
import us.chiraq.practicepots.profile.ProfileManager;
import us.chiraq.practicepots.tasks.EXPBarTask;
import us.chiraq.practicepots.utils.Data;
import us.chiraq.practicepots.utils.EloCalculator;
import us.chiraq.practicepots.utils.InventorySave;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

public class DuelListeners
implements Listener {
    private Nanny main = Nanny.getInstance();
    private EntityHider eh;
    private LangFile lf = this.main.getLangFile();
    private ConfigFile cf = main.getConfigFile();
    
    private float kb;
    
    private ProfileManager pm = this.main.getProfileManager();
    
    public DuelListeners(EntityHider eh) {
    	this.eh = eh;
    	this.kb = (float) cf.getConfiguration().getDouble("KNOCKBACK.BASE");
    }
    
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(final ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player)) return;
        Player player = (Player)e.getEntity().getShooter();
        Profile profile = Profile.getProfile(player.getUniqueId());
        if (profile.getDuel() != null) {
            Duel duel = profile.getDuel();
            duel.getProfile1().getProjectiles().add((Entity)e.getEntity());
            duel.getProfile2().getProjectiles().add((Entity)e.getEntity());
            Player[] arrplayer = Bukkit.getOnlinePlayers();
            int n = arrplayer.length;
            int n2 = 0;
            while (n2 < n) {
                final Player online = arrplayer[n2];
                if (online != duel.getPlayer1() && online != duel.getPlayer2()) {
                	eh.hideEntity(online, e.getEntity());
                	((CraftPlayer)online).getHandle().playerConnection.sendPacket((Packet)new PacketPlayOutEntityDestroy(new int[]{e.getEntity().getEntityId()}));
                }
                ++n2;
            }
            profile.getDuel().addActiveEntity(e.getEntity());
            return;
        }
        if (profile.getTeam() != null && profile.getTeam().getDuel() != null) {
            TeamDuel teamDuel = profile.getTeam().getDuel();
            for (Player all : teamDuel.getAllPlayers()) {
                Profile allProf = Profile.getProfile(all.getUniqueId());
                allProf.getProjectiles().add((Entity)e.getEntity());
            }
            return;
        }
        profile.getProjectiles().add((Entity)e.getEntity());
        Player[] teamDuel = Bukkit.getOnlinePlayers();
        int n = teamDuel.length;
        int all = 0;
        while (all < n) {
            final Player online = teamDuel[all];
            if (online != player) {
            	eh.hideEntity(online, e.getEntity());
                ((CraftPlayer)online).getHandle().playerConnection.sendPacket((Packet)new PacketPlayOutEntityDestroy(new int[]{e.getEntity().getEntityId()}));
            }
            ++all;
        }
        profile.getDuel().addActiveEntity(e.getEntity());
    }
    
    @SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(final PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        Profile profile = Profile.getProfile(player.getUniqueId());
        if (profile.getDuel() != null) {
            Duel duel = profile.getDuel();
            duel.getProfile1().getDrops().add((Entity)e.getItemDrop());
            duel.getProfile2().getDrops().add((Entity)e.getItemDrop());
            for (final Player online : Bukkit.getOnlinePlayers()) {
                if (online == duel.getPlayer1() || online == duel.getPlayer2()) continue;
                    ((CraftPlayer)online).getHandle().playerConnection.sendPacket((Packet)new PacketPlayOutEntityDestroy(new int[]{e.getItemDrop().getEntityId()}));
            }
        } else {
            if (profile.getTeam() != null && profile.getTeam().isInFight() && profile.getTeam().getDuel() != null) {
                for (Player teamMember : profile.getTeam().getDuel().getAllPlayers()) {
                    Profile teamProfile = Profile.getProfile(teamMember.getUniqueId());
                    teamProfile.getDrops().add((Entity)e.getItemDrop());
                }
                for (final Player online : Bukkit.getOnlinePlayers()) {
                    if (profile.getTeam().getDuel().getAllPlayers().contains((Object)online)) continue;
                        ((CraftPlayer)online).getHandle().playerConnection.sendPacket((Packet)new PacketPlayOutEntityDestroy(new int[]{e.getItemDrop().getEntityId()}));
                }
            }
            for (final Player online : Bukkit.getOnlinePlayers()) {
                if (online == player) continue;
                    ((CraftPlayer)online).getHandle().playerConnection.sendPacket((Packet)new PacketPlayOutEntityDestroy(new int[]{e.getItemDrop().getEntityId()}));
            }
        }
    }

    @SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onSplash(PotionSplashEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {
            Player player = (Player)e.getEntity().getShooter();
            Profile profile = Profile.getProfile(player.getUniqueId());
            e.getAffectedEntities().removeAll(Arrays.asList(Bukkit.getOnlinePlayers()));
            if (profile.getDuel() != null) {
                e.getAffectedEntities().add(profile.getDuel().getPlayer1());
                e.getAffectedEntities().add(profile.getDuel().getPlayer2());
            } else {
                Team team = profile.getTeam();
                if (team != null && team.getDuel() != null) {
                    e.getAffectedEntities().addAll(team.getDuel().getAllPlayers());
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
	@EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
        	Profile pro = Profile.getProfile(((Player)e.getEntity()).getUniqueId());
        	if (pro.isInvulnerability()) {
        		e.setCancelled(true);
        		return;
        	}
        	if (pro.isInSpectator()) {
        		e.setCancelled(true);
        		return;
        	}
        	if (e.getDamager() instanceof Player) {
        		Player attacked = (Player) e.getEntity();
        		Player attacker = (Player) e.getDamager();
        		
        		attacked.setVelocity(attacked.getVelocity().add(attacked.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize().multiply(kb)));
        		
        		Profile damager = Profile.getProfile(((Player)e.getDamager()).getUniqueId());
        		if (damager.isInSpectator()) {
        			e.setCancelled(true);
        			return;
        		}
        		if (damager.getTeam() != null) {
        			damager.getTeam().getMembers().contains((Object)e.getEntity());
        			e.setCancelled(true);
        			return;
        		}
        	} else if (e.getDamager() instanceof Projectile && ((Projectile)e.getDamager()).getShooter() instanceof Player) {
        		Profile damager = Profile.getProfile(((Player)((Projectile)e.getDamager()).getShooter()).getUniqueId());
        		if (damager.getTeam() != null && damager.getTeam().getMembers().contains((Object)e.getEntity())) {
        			e.setCancelled(true);
        			return;
        		}
        		
        	}
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Profile profile = Profile.getProfile(player.getUniqueId());
        Profile.getOnlineProfiles().remove(profile);
        profile.setQueueCooldown(false);
        if (profile.getQueue() != null) {
        	profile.getSearchingLadder().getRankedQueue().remove((Object)player);
        	profile.getSearchingLadder().getUnrankedQueue().remove((Object)player);
        	profile.getSearchingLadder().getPremiumRankedQueue().remove((Object)player);
        	profile.getQueue().cancel();
        }
        if (!profile.isInSpawn()) new InventorySave(player);
        if (profile.isInSpectator()) {
        	profile.setInSpectator(false);
        	profile.setSpectating(null);
        	player.setGameMode(GameMode.SURVIVAL);
        	return;
        }
        if (profile.getTeam() != null) {
            if (profile.getTeam().getLeader().getName().equalsIgnoreCase(player.getName())) {
                profile.getTeam().sendMessage(this.lf.getString("TEAM.DELETED"));
                profile.getTeam().delete();
                return;
            }
            profile.getTeam().sendMessage(this.lf.getString("TEAM.LEFT").replace("%PLAYER%", player.getName()));
            profile.getTeam().removePlayer(player);
            //profile.getTeam().resetScoreboard(player);
            if (profile.getTeam().getDuel() != null) {
                TeamDuel duel = profile.getTeam().getDuel();
                if (duel.getTeam1().equals(profile.getTeam())) {
                    duel.setTeam1Left(duel.getTeam1Left() - 1);
                } else {
                    duel.setTeam2Left(duel.getTeam2Left() - 1);
                }
                if (duel.getTeam1Left() == 0) {
                    duel.setWinner(duel.getTeam2());
                } else if (duel.getTeam2Left() == 0) {
                    duel.setWinner(duel.getTeam1());
                }
                for (Entity ent : duel.getActiveEntities()) {
                	eh.setVisibility(player, ent.getEntityId(), false);
                }
            }
            return;
        }
        if (profile.isInSpectator()) {
        	Profile.getProfile(profile.getSpectatingPlayer().getUniqueId()).removeSpectator(player);
        	profile.setSpectating(null);
        	profile.setInSpectator(true);
        }
        profile.clearSpectators();
        if (profile.getDuel() != null) {
            Duel duel = profile.getDuel();
            duel.getProfile1().setInArena(false);
            duel.getProfile1().setInSpawn(true);
            duel.getProfile2().setInArena(false);
            duel.getProfile2().setInSpawn(true);
            final Player otherPlayer = duel.getPlayer1() != player ? duel.getPlayer1() : duel.getPlayer2();
            Profile otherProfile = Profile.getProfile(otherPlayer.getUniqueId());
            new InventorySave(otherPlayer);
            if (duel.getRanked() == 0) {
                int otherElo = otherProfile.getRank().get(duel.getLadder());
                int elo = profile.getRank().get(duel.getLadder());
                int[] results = EloCalculator.getNewRankings(otherElo, elo, true);
                otherPlayer.sendMessage(this.lf.getString("QUEUE.SEARCH.RANKED.ELO_CHANGE").replace("%WINNER%", otherPlayer.getName()).replace("%WINNER_ELO%", "" + results[0] + "").replace("%WINNER_AMOUNT%", "" + (results[0] - otherElo) + "").replace("%LOSER%", player.getName()).replace("%LOSER_ELO%", "" + results[1] + "").replace("%LOSER_AMOUNT%", "" + (elo - results[1]) + ""));
                player.sendMessage(this.lf.getString("QUEUE.SEARCH.RANKED.ELO_CHANGE").replace("%WINNER%", otherPlayer.getName()).replace("%WINNER_ELO%", "" + results[0] + "").replace("%WINNER_AMOUNT%", "" + (results[0] - otherElo) + "").replace("%LOSER%", player.getName()).replace("%LOSER_ELO%", "" + results[1] + "").replace("%LOSER_AMOUNT%", "" + (elo - results[1]) + ""));
                profile.getRank().put(duel.getLadder(), results[1]);
                otherProfile.getRank().put(duel.getLadder(), results[0]);
                proccessStats(otherProfile, profile, duel, 1);
            } else if (duel.getRanked() == 2) {
                int otherElo = otherProfile.getRank().get(duel.getLadder());
                int elo = profile.getRank().get(duel.getLadder());
                int[] results = EloCalculator.getNewRankings(otherElo, elo, true);
                otherPlayer.sendMessage(this.lf.getString("QUEUE.SEARCH.PREMIUMRANKED.ELO_CHANGE").replace("%WINNER%", otherPlayer.getName()).replace("%WINNER_ELO%", "" + results[0] + "").replace("%WINNER_AMOUNT%", "" + (results[0] - otherElo) + "").replace("%LOSER%", player.getName()).replace("%LOSER_ELO%", "" + results[1] + "").replace("%LOSER_AMOUNT%", "" + (elo - results[1]) + ""));
                player.sendMessage(this.lf.getString("QUEUE.SEARCH.PREMIUMRANKED.ELO_CHANGE").replace("%WINNER%", otherPlayer.getName()).replace("%WINNER_ELO%", "" + results[0] + "").replace("%WINNER_AMOUNT%", "" + (results[0] - otherElo) + "").replace("%LOSER%", player.getName()).replace("%LOSER_ELO%", "" + results[1] + "").replace("%LOSER_AMOUNT%", "" + (elo - results[1]) + ""));
                profile.getRank().put(duel.getLadder(), results[1]);
                profile.getRankedLosses().put(duel.getLadder(), profile.getRankedLosses().get(duel.getLadder()) + 1);
                proccessStats(otherProfile, profile, duel, 2);
            } else if (duel.getRanked() == 1) {
                proccessStats(otherProfile, profile, duel, 3);
            }
            player.sendMessage(this.lf.getString("QUEUE.FINISH.WINNER").replace("%WINNER%", otherPlayer.getName()));
            otherPlayer.sendMessage(this.lf.getString("QUEUE.FINISH.WINNER").replace("%WINNER%", otherPlayer.getName()));
            FancyMessage fancyMessage = new FancyMessage(this.lf.getString("QUEUE.FINISH.INVENTORY_VIEW")).then((Object)ChatColor.YELLOW + otherPlayer.getName()).command("/_ " + otherPlayer.getUniqueId().toString()).then((Object)ChatColor.YELLOW + ", " + player.getName() + ".").command("/_ " + player.getUniqueId().toString());
            fancyMessage.send(player);
            fancyMessage.send(otherPlayer);
            for (Entity ent : duel.getActiveEntities()) {
            	duel.removeActiveEntity(ent);
            	ent.remove();
            }
            if (duel.getTask() != null) {
                duel.getTask().cancel();
            }
            duel.getProfile1().setDuel(null);
            duel.getProfile2().setDuel(null);

            DuelListeners.this.pm.sendToSpawn(otherPlayer);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e)
    {
      e.getDrops().clear();
      e.setDroppedExp(0);
      
      final Player player = e.getEntity();
      
      /*PlayerScoreboard scoreboard = PlayerScoreboard.getScoreboard(player);
      if (scoreboard.getEntry("enderpearl") != null) {
        scoreboard.getEntry("enderpearl").setCancelled(true);
      }*/
      Profile profile = Profile.getProfile(player.getUniqueId());
      
      new InventorySave(player);
      if ((profile.getTeam() != null) && (profile.getTeam().getDuel() != null))
      {
        TeamDuel duel = profile.getTeam().getDuel();
        duel.getTeam1().sendMessage(e.getDeathMessage());
        duel.getTeam2().sendMessage(e.getDeathMessage());
        if (duel.getTeam1().equals(profile.getTeam())) {
          duel.setTeam1Left(duel.getTeam1Left() - 1);
        } else {
          duel.setTeam2Left(duel.getTeam2Left() - 1);
        }
        profile.setInSpawn(true);
        profile.setInArena(false);
        if (duel.getTeam1Left() == 0) {
          duel.setWinner(duel.getTeam2());
        } else if (duel.getTeam2Left() == 0) {
          duel.setWinner(duel.getTeam1());
        }
        //profile.getTeam().resetScoreboard(player);
        
        for (Entity ent : duel.getActiveEntities()) {
        	eh.setVisibility(player, ent.getEntityId(), false);
        }
        
        new BukkitRunnable()
        {
          public void run()
          {
            ((CraftPlayer)player).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
          }
        }
        
          .runTaskLater(this.main, 20L);
      }
      if (profile.getDuel() != null)
      {
        Duel duel = profile.getDuel();
        
        duel.getProfile1().setInArena(false);
        duel.getProfile1().setInSpawn(true);
        
        duel.getProfile2().setInArena(false);
        duel.getProfile2().setInSpawn(true);
        Player otherPlayer;
        if (duel.getPlayer1() != player) {
          otherPlayer = duel.getPlayer1();
        } else {
          otherPlayer = duel.getPlayer2();
        }
        Profile otherProfile = Profile.getProfile(otherPlayer.getUniqueId());
        
        /*PlayerScoreboard scoreboard1 = duel.getScoreboard1();
        PlayerScoreboard scoreboard2 = duel.getScoreboard2();
        for (String string : this.lf.getStringList("SCOREBOARD.MATCH_INFORMATION"))
        {
          if (scoreboard1.getEntry(string) != null) {
            scoreboard1.getEntry(string).setCancelled(true);
          }
          if (scoreboard2.getEntry(string) != null) {
            scoreboard2.getEntry(string).setCancelled(true);
          }
        }*/
        new InventorySave(otherPlayer);
        if (duel.getRanked() == 0)
        {
          int otherElo = ((Integer)otherProfile.getRank().get(duel.getLadder())).intValue();
          int elo = ((Integer)profile.getRank().get(duel.getLadder())).intValue();
          
          int[] results = EloCalculator.getNewRankings(otherElo, elo, true);
          
          otherPlayer.sendMessage(this.lf.getString("QUEUE.SEARCH.RANKED.ELO_CHANGE")
            .replace("%WINNER%", otherPlayer.getName())
            .replace("%WINNER_ELO%", results[0] + "")
            .replace("%WINNER_AMOUNT%", results[0] - otherElo + "")
            .replace("%LOSER%", player.getName())
            .replace("%LOSER_ELO%", results[1] + "")
            .replace("%LOSER_AMOUNT%", elo - results[1] + ""));
          player.sendMessage(this.lf.getString("QUEUE.SEARCH.RANKED.ELO_CHANGE")
            .replace("%WINNER%", otherPlayer.getName())
            .replace("%WINNER_ELO%", results[0] + "")
            .replace("%WINNER_AMOUNT%", results[0] - otherElo + "")
            .replace("%LOSER%", player.getName())
            .replace("%LOSER_ELO%", results[1] + "")
            .replace("%LOSER_AMOUNT%", elo - results[1] + ""));
          
          profile.getRank().put(duel.getLadder(), Integer.valueOf(results[1]));
          otherProfile.getRank().put(duel.getLadder(), Integer.valueOf(results[0]));
          proccessStats(otherProfile, profile, duel, 1);
        }
        else if (duel.getRanked() == 2)
        {
        	int otherElo = ((Integer)otherProfile.getRank().get(duel.getLadder())).intValue();
            int elo = ((Integer)profile.getRank().get(duel.getLadder())).intValue();
            
            int[] results = EloCalculator.getNewRankings(otherElo, elo, true);
            
            otherPlayer.sendMessage(this.lf.getString("QUEUE.SEARCH.PREMIUMRANKED.ELO_CHANGE")
              .replace("%WINNER%", otherPlayer.getName())
              .replace("%WINNER_ELO%", results[0] + "")
              .replace("%WINNER_AMOUNT%", results[0] - otherElo + "")
              .replace("%LOSER%", player.getName())
              .replace("%LOSER_ELO%", results[1] + "")
              .replace("%LOSER_AMOUNT%", elo - results[1] + ""));
            player.sendMessage(this.lf.getString("QUEUE.SEARCH.PREMIUMRANKED.ELO_CHANGE")
              .replace("%WINNER%", otherPlayer.getName())
              .replace("%WINNER_ELO%", results[0] + "")
              .replace("%WINNER_AMOUNT%", results[0] - otherElo + "")
              .replace("%LOSER%", player.getName())
              .replace("%LOSER_ELO%", results[1] + "")
              .replace("%LOSER_AMOUNT%", elo - results[1] + ""));
            
            profile.getRank().put(duel.getLadder(), Integer.valueOf(results[1]));
            otherProfile.getRank().put(duel.getLadder(), Integer.valueOf(results[0]));
            proccessStats(otherProfile, profile, duel, 2);
        } else if (duel.getRanked() == 1) {
        	proccessStats(otherProfile, profile, duel, 3);
        }
        player.sendMessage(this.lf.getString("QUEUE.FINISH.WINNER").replace("%WINNER%", otherPlayer.getName()));
        otherPlayer.sendMessage(this.lf.getString("QUEUE.FINISH.WINNER").replace("%WINNER%", otherPlayer.getName()));
        FancyMessage fancyMessage = new FancyMessage(this.lf.getString("QUEUE.FINISH.INVENTORY_VIEW")).then(ChatColor.YELLOW + otherPlayer.getName()).command("/_ " + otherPlayer.getUniqueId().toString()).then(ChatColor.YELLOW + ", " + player.getName() + ".").command("/_ " + player.getUniqueId().toString());
        
        fancyMessage.send(player);
        fancyMessage.send(otherPlayer);
        
        new BukkitRunnable()
        {
          public void run()
          {
            ((CraftPlayer)player).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
            DuelListeners.this.pm.sendToSpawn(otherPlayer);
          }
        }
        
          .runTaskLater(this.main, 20L);
      }
      e.setDeathMessage(null);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
    	if (e.getCause() == TeleportCause.ENDER_PEARL) {
    		Profile profile = Profile.getProfile(e.getPlayer().getUniqueId());
    		if (!profile.isInArena()) {
    			e.setCancelled(true);
    			return;
    		}
    	}
    }
    
	@EventHandler
    public void onPearl(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        //PlayerScoreboard scoreboard = PlayerScoreboard.getScoreboard(player);
        Profile pro = Profile.getProfile(player.getUniqueId());
        if (pro.getDuel() != null) {
        	if (pro.getDuel().isCountdown()) {
        		e.setCancelled(true);
        		player.sendMessage(ChatColor.RED + "The duel has not started yet!");
        		return;
        	}
        }
        if (e.getItem() != null && e.getItem().getType() == Material.ENDER_PEARL) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                e.setCancelled(true);
                player.updateInventory();
                return;
            }
            if (e.getAction() == Action.RIGHT_CLICK_AIR) {
            	if (player.getLevel() != 0) {
            		e.setCancelled(true);
            		player.updateInventory();
            		player.sendMessage(ChatColor.YELLOW + "You must wait " + ChatColor.RED + player.getLevel() + ChatColor.YELLOW + " before enderpearling again!");
            		return;
            	} else {
            		player.setLevel(16);
            		new EXPBarTask(player, pro).runTaskTimerAsynchronously(main, 0L, 20L);
            		return;
            	}
                /*Entry entry = scoreboard.getEntry("enderpearl");
                if (entry == null) {
                    //new Entry("enderpearl", scoreboard).setText(this.lf.getString("SCOREBOARD.ENDERPEARL")).setCountdown(true).setTime(16.0).send();
                	Entry en = new Entry("enderpearl", scoreboard).setText(this.lf.getString("SCOREBOARD.ENDERPEARL"));
                	en.setCountdown(true);
                	en.setTime(16.0);
                	en.send();
                } else {
                    e.setCancelled(true);
                    player.updateInventory();
                    player.sendMessage((Object)ChatColor.YELLOW + "You must wait " + (Object)ChatColor.RED + entry.getTextTime() + (Object)ChatColor.YELLOW + " before enderpearling again!");
                }*/
            	
            }
        }
    }

    public void proccessStats(Profile winner, Profile loser, Duel duel, int type) {
    	if (type == 1) {
        	loser.getRankedLosses().put(duel.getLadder(), loser.getRankedLosses().get(duel.getLadder()) + 1);
            winner.getRankedWins().put(duel.getLadder(), winner.getRankedWins().get(duel.getLadder()) + 1);
            winner.setGlobalElo(Data.calculateGlobalElo(winner));
            loser.setGlobalElo(Data.calculateGlobalElo(loser));
            Data.saveProfile(loser);
            Data.saveProfile(winner);
        	winner.setTotalMatches(winner.getTotalMatches() + 1);
        	loser.setTotalMatches(loser.getTotalMatches() + 1);
        	duel.getLadder().setCurrentRankedMatches(duel.getLadder().getCurrentRankedMatches() - 1);
    	} else if (type == 2) {
        	loser.getRankedLosses().put(duel.getLadder(), loser.getRankedLosses().get(duel.getLadder()) + 1);
            winner.getRankedWins().put(duel.getLadder(), winner.getRankedWins().get(duel.getLadder()) + 1);
            winner.setGlobalElo(Data.calculateGlobalElo(winner));
            loser.setGlobalElo(Data.calculateGlobalElo(loser));
            Data.saveProfile(loser);
            Data.saveProfile(winner);
        	winner.setTotalMatches(winner.getTotalMatches() + 1);
        	loser.setTotalMatches(loser.getTotalMatches() + 1);
            duel.getLadder().setCurrentPremiumRankedMatches(duel.getLadder().getCurrentPremiumRankedMatches()-1);
    	} else {
    		loser.getUnRankedLosses().put(duel.getLadder(), loser.getUnRankedLosses().get(duel.getLadder()) + 1);
            winner.getUnRankedWins().put(duel.getLadder(), winner.getUnRankedWins().get(duel.getLadder()) + 1);
            duel.getLadder().setCurrentUnRankedMatches(duel.getLadder().getCurrentUnRankedMatches()-1);
    	}
    	
    	for (Player player : winner.getSpectatingPlayers()) {
    		player.setGameMode(GameMode.SURVIVAL);
    		DuelListeners.this.pm.sendToSpawn(player);
    	}
    	for (Player player : loser.getSpectatingPlayers()) {
    		player.setGameMode(GameMode.SURVIVAL);
    		DuelListeners.this.pm.sendToSpawn(player);
    	}
    	
    	winner.setQueueCooldown(true);
    	loser.setQueueCooldown(true);
    	
    	new BukkitRunnable() {
    		public void run() {
    	    	winner.setQueueCooldown(false);
    	    	loser.setQueueCooldown(false);
    		}
    	}.runTaskLater(main, 40L);
    }
    
}

