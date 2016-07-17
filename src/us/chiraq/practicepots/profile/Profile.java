package us.chiraq.practicepots.profile;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.ConfigFile;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Arena;
import us.chiraq.practicepots.game.Ladder;
import us.chiraq.practicepots.game.Team;
import us.chiraq.practicepots.game.fight.Duel;
import us.chiraq.practicepots.game.kit.Kit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Profile {
    private static Set<Profile> profiles = new HashSet<Profile>();
    private LangFile lf = Nanny.getInstance().getLangFile();
    private ConfigFile cf = Nanny.getInstance().getConfigFile();
    private UUID uuid;
    private List<Kit> kits;
    private double globalElo;
    private int totalMatches;
    private Map<Ladder, Integer> rank;
    private Map <Ladder, Integer> rankedWins;
    private Map <Ladder, Integer> rankedLosses;
    private Map <Ladder, Integer> unRankedWins;
    private Map <Ladder, Integer> unRankedLosses;
    private boolean inSpawn;
    private boolean inArena;
    private boolean inKitEditor;
    private boolean inSpectator;
    private boolean queueCooldown;
    private Player spectating;
    private Ladder searchingLadder;
    private int searchingRange;
    private BukkitTask queue;
    private int attempts;
    private Duel duel;
    private Inventory duelInventory;
    private Player duelPlayer;
    private Map<Player, Ladder> duelRequests;
    private Ladder editingLadder;
    private Team team;
    private Kit currentKit;
    private Set<Entity> projectiles;
    private Set<Entity> drops;
    private Set<Player> damaged;
    private Ladder selected;
    private Arena arena;

    private List<Player> spectatingPlayers;
    
    private boolean invulnerability;
    private boolean showPlayers;
    
    public Profile(UUID uuid) {
        this.uuid = uuid;
        this.kits = new ArrayList<Kit>();
        this.duelRequests = new HashMap<Player, Ladder>();
        this.globalElo = 0;
        this.totalMatches = 0;
        this.rank = new HashMap<Ladder, Integer>();
        this.rankedWins = new HashMap<Ladder, Integer>();
        this.rankedLosses = new HashMap<Ladder, Integer>();
        this.unRankedWins = new HashMap<Ladder, Integer>();
        this.unRankedLosses = new HashMap<Ladder, Integer>();
        this.projectiles = new HashSet<Entity>();
        this.drops = new HashSet<Entity>();
        this.spectatingPlayers = new ArrayList<Player>();
        for (Ladder ladder : Ladder.getLadders()) {
            if (this.rank.containsKey(ladder)) continue;
            this.rank.put(ladder, ladder.getDefaultElo());
        }
        for (Ladder ladder : Ladder.getLadders()) {
            if (rankedWins.containsKey(ladder)) continue;
            rankedWins.put(ladder, 0);
        }
        for (Ladder ladder : Ladder.getLadders()) {
            if (rankedLosses.containsKey(ladder)) continue;
            rankedLosses.put(ladder, 0);
        }
        for (Ladder ladder : Ladder.getLadders()) {
            if (unRankedWins.containsKey(ladder)) continue;
            unRankedWins.put(ladder, 0);
        }
        for (Ladder ladder : Ladder.getLadders()) {
            if (unRankedLosses.containsKey(ladder)) continue;
            unRankedLosses.put(ladder, 0);
        }
        this.damaged = new HashSet<Player>();
        this.invulnerability = false;
        this.showPlayers = false;
        this.inSpectator = false;
        this.setQueueCooldown(false);
        this.spectating = null;
        this.selected = null;
        this.arena = null;
        Profile.getProfiles().add(this);
    }

    public void putInQueue(final Ladder ladder, final int ranked) {
        final Player player = Bukkit.getPlayer((UUID)this.uuid);
        if (player != null) {
            final int elo = this.rank.get(ladder);
            this.searchingRange = this.cf.getInt("QUEUE.STARTING_RANGE");
            this.searchingLadder = ladder;
            this.attempts = 0;
            if (ranked == 0) {
                player.sendMessage(this.lf.getString("QUEUE.SEARCH.RANKED.START").replace("%ELO%", "" + elo + "").replace("%LADDER%", ladder.getName()));
                ladder.getRankedQueue().add(player);
            } else if (ranked == 2) {
            	player.sendMessage(this.lf.getString("QUEUE.SEARCH.PREMIUMRANKED.START").replace("%ELO%", "" + elo + "").replace("%LADDER%", ladder.getName()));
            	ladder.getPremiumRankedQueue().add(player);
            }
            else 
            {
                player.sendMessage(this.lf.getString("QUEUE.SEARCH.UNRANKED.START").replace("%LADDER%", ladder.getName()));
                ladder.getUnrankedQueue().add(player);
            }
            this.queue = new BukkitRunnable(){

                public void run() {
                    if (ranked == 0) {
                        if (Profile.this.searchingRange >= Profile.this.cf.getInt("QUEUE.MAX_RANGE") || elo - (Profile.this.searchingRange + Profile.this.cf.getInt("QUEUE.RANGE_INCREASE")) <= 0) {
                            player.sendMessage(Profile.this.lf.getString("QUEUE.SEARCH.RANKED.NO_MATCH_FOUND"));
                            Profile.this.getSearchingLadder().getRankedQueue().remove((Object)player);
                            Nanny.getInstance().getProfileManager().sendToSpawn(player);
                            this.cancel();
                            return;
                        }
                        int highRange = elo + Profile.this.searchingRange;
                        int lowRange = elo - Profile.this.searchingRange;
                        player.sendMessage(Profile.this.lf.getString("QUEUE.SEARCH.RANKED.SEARCHING").replace("%ELO%", "" + elo + "").replace("%LADDER%", ladder.getName()).replace("%HIGH_RANGE%", "" + highRange + "").replace("%LOW_RANGE%", "" + lowRange + ""));
                        for (Player otherPlayer : ladder.getRankedQueue()) {
                            if (otherPlayer == player) continue;
                            Profile profile = Profile.getProfile(otherPlayer.getUniqueId());
                            int otherElo = profile.getRank().get(ladder);
                            int otherHighRange = otherElo + profile.getSearchingRange();
                            int otherLowRange = otherElo - profile.getSearchingRange();
                            if (elo > otherHighRange || elo < otherLowRange || otherElo > highRange || otherElo < lowRange) continue;
                            player.sendMessage(Profile.this.lf.getString("QUEUE.SEARCH.RANKED.FOUND").replace("%OPPONENT%", otherPlayer.getName()).replace("%ELO%", "" + otherElo + ""));
                            otherPlayer.sendMessage(Profile.this.lf.getString("QUEUE.SEARCH.RANKED.FOUND").replace("%OPPONENT%", player.getName()).replace("%ELO%", "" + elo + ""));
                            if (profile.getQueue() != null) {
                                profile.getQueue().cancel();
                            }
                            if (ladder.getArenas().isEmpty()) {
                                player.sendMessage(Profile.this.lf.getString("ERROR"));
                                otherPlayer.sendMessage(Profile.this.lf.getString("ERROR"));
                                return;
                            }
                            new Duel(player, otherPlayer, Profile.this, profile, ladder, ladder.getArenas().get(new Random().nextInt(ladder.getArenas().size())), 0); //TODO: Come back later
                            this.cancel();
                            Profile.this.queue = null;
                            profile.setQueue(null);
                            return;
                        }
                        Profile.this.searchingRange = Profile.this.searchingRange + Profile.this.cf.getInt("QUEUE.RANGE_INCREASE");
                    } else if (ranked == 2)
                    	{
                    		if (Profile.this.searchingRange >= Profile.this.cf.getInt("QUEUE.MAX_RANGE") || elo - (Profile.this.searchingRange + Profile.this.cf.getInt("QUEUE.RANGE_INCREASE")) <= 0) {
                    			player.sendMessage(Profile.this.lf.getString("QUEUE.SEARCH.PREMIUMRANKED.NO_MATCH_FOUND"));
                    			Profile.this.getSearchingLadder().getRankedQueue().remove((Object)player);
                                Nanny.getInstance().getProfileManager().sendToSpawn(player);
                                this.cancel();
                                return;
                    		}
                    		int highRange = elo + Profile.this.searchingRange;
                    		int lowRange = elo - Profile.this.searchingRange;
                    		player.sendMessage(Profile.this.lf.getString("QUEUE.SEARCH.PREMIUMRANKED.SEARCHING").replace("%ELO%", "" + elo + "").replace("%LADDER%", ladder.getName()).replace("%HIGH_RANGE%", "" + highRange + "").replace("%LOW_RANGE%", "" + lowRange + ""));
                    		for (Player otherPlayer : ladder.getPremiumRankedQueue()) {
                    			if (otherPlayer == player) continue;
                                Profile profile = Profile.getProfile(otherPlayer.getUniqueId());
                                int otherElo = profile.getRank().get(ladder);
                                int otherHighRange = otherElo + profile.getSearchingRange();
                                int otherLowRange = otherElo - profile.getSearchingRange();
                                if (elo > otherHighRange || elo < otherLowRange || otherElo > highRange || otherElo < lowRange) continue;
                                player.sendMessage(Profile.this.lf.getString("QUEUE.SEARCH.PREMIUMRANKED.FOUND").replace("%OPPONENT%", otherPlayer.getName()).replace("%ELO%", "" + otherElo + ""));
                                otherPlayer.sendMessage(Profile.this.lf.getString("QUEUE.SEARCH.PREMIUMRANKED.FOUND").replace("%OPPONENT%", player.getName()).replace("%ELO%", "" + elo + ""));
                                if (profile.getQueue() != null) {
                                    profile.getQueue().cancel();
                                }
                                if (ladder.getArenas().isEmpty()) {
                                    player.sendMessage(Profile.this.lf.getString("ERROR"));
                                    otherPlayer.sendMessage(Profile.this.lf.getString("ERROR"));
                                    return;
                                }
                                new Duel(player, otherPlayer, Profile.this, profile, ladder, ladder.getArenas().get(new Random().nextInt(ladder.getArenas().size())), 2);
                                this.cancel();
                                Profile.this.queue = null;
                                profile.setQueue(null);
                                return;
                    		}
                    		Profile.this.searchingRange = Profile.this.searchingRange + Profile.this.cf.getInt("QUEUE.RANGE_INCREASE");
                    	} else {
                    		if (Profile.this.attempts >= Profile.this.cf.getInt("QUEUE.UNRANKED_ATTEMPTS")) {
                            	player.sendMessage(Profile.this.lf.getString("QUEUE.SEARCH.UNRANKED.NO_MATCH_FOUND"));
                            	Profile.this.getSearchingLadder().getUnrankedQueue().remove((Object)player);
                            	Nanny.getInstance().getProfileManager().sendToSpawn(player);
                            	this.cancel();
                            	return;
                        	   }
                        player.sendMessage(Profile.this.lf.getString("QUEUE.SEARCH.UNRANKED.SEARCHING").replace("%LADDER%", ladder.getName()));
                        Profile.this.attempts = Profile.this.attempts + 1;
                        for (Player otherPlayer : ladder.getUnrankedQueue()) {
                            if (otherPlayer == player) continue;
                            Profile profile = Profile.getProfile(otherPlayer.getUniqueId());
                            player.sendMessage(Profile.this.lf.getString("QUEUE.SEARCH.UNRANKED.FOUND").replace("%OPPONENT%", otherPlayer.getName()));
                            otherPlayer.sendMessage(Profile.this.lf.getString("QUEUE.SEARCH.UNRANKED.FOUND").replace("%OPPONENT%", player.getName()));
                            if (profile.getQueue() != null) {
                                profile.getQueue().cancel();
                            }
                            if (ladder.getArenas().isEmpty()) {
                                player.sendMessage(Profile.this.lf.getString("ERROR"));
                                otherPlayer.sendMessage(Profile.this.lf.getString("ERROR"));
                                return;
                            }
                            Nanny.getInstance().getProfileManager().hidePlayerFromAll(player);
                            Nanny.getInstance().getProfileManager().hidePlayerFromAll(otherPlayer);
                            new Duel(player, otherPlayer, Profile.this, profile, ladder, ladder.getArenas().get(new Random().nextInt(ladder.getArenas().size())), 1); //TODO: Come back later
                            this.cancel();
                            Profile.this.queue = null;
                            profile.setQueue(null);
                            return;
                        }
                    }
                }
            }.runTaskTimer((Plugin)Nanny.getInstance(), 0, 200);
        }
    }
    
    public int calculateTotalFromHashMap(Map<Ladder, Integer> map) {
    	int total = 0;
    	for (int i : map.values()) {
    		total = total + i;
    	}
    	return total;
    }
    
    public void setRank(Map<Ladder, Integer> rank) {
        this.rank = rank;
        for (Ladder ladder : Ladder.getLadders()) {
            if (rank.containsKey(ladder)) continue;
            rank.put(ladder, ladder.getDefaultElo());
        }
    }

    public Kit getKit(String name, Ladder ladder) {
        for (Kit kit : this.getKits()) {
            if (!kit.getName().equals(name) || !kit.getLadder().equals(ladder)) continue;
            return kit;
        }
        return null;
    }

    public List<Kit> getKits(Ladder ladder) {
        ArrayList<Kit> toReturn = new ArrayList<Kit>();
        for (Kit kit : this.getKits()) {
            if (kit.getLadder() == null || !kit.getLadder().equals(ladder)) continue;
            toReturn.add(kit);
        }
        return toReturn;
    }

    public static Profile getProfile(UUID uuid) {
        for (Profile profile : Profile.getProfiles()) {
            if (!profile.getUuid().equals(uuid)) continue;
            return profile;
        }
        return null;
    }

    public static Set<Profile> getProfiles() {
        return profiles;
    }

    public LangFile getLf() {
        return this.lf;
    }

    public ConfigFile getCf() {
        return this.cf;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public List<Kit> getKits() {
        return this.kits;
    }

    public Map<Ladder, Integer> getRank() {
        return this.rank;
    }

    public boolean isInSpawn() {
        return this.inSpawn;
    }

    public boolean isInArena() {
        return this.inArena;
    }

    public boolean isInKitEditor() {
        return this.inKitEditor;
    }

    public Ladder getSearchingLadder() {
        return this.searchingLadder;
    }

    public int getSearchingRange() {
        return this.searchingRange;
    }

    public BukkitTask getQueue() {
        return this.queue;
    }

    public int getAttempts() {
        return this.attempts;
    }

    public Duel getDuel() {
        return this.duel;
    }

    public Inventory getDuelInventory() {
        return this.duelInventory;
    }

    public Player getDuelPlayer() {
        return this.duelPlayer;
    }

    public Map<Player, Ladder> getDuelRequests() {
        return this.duelRequests;
    }

    public Ladder getEditingLadder() {
        return this.editingLadder;
    }

    public Team getTeam() {
        return this.team;
    }

    public Kit getCurrentKit() {
        return this.currentKit;
    }

    public Set<Entity> getProjectiles() {
        return this.projectiles;
    }

    public Set<Entity> getDrops() {
        return this.drops;
    }

    public Set<Player> getDamaged() {
        return this.damaged;
    }

    public void setLf(LangFile lf) {
        this.lf = lf;
    }

    public void setCf(ConfigFile cf) {
        this.cf = cf;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setKits(List<Kit> kits) {
        this.kits = kits;
    }

    public void setInSpawn(boolean inSpawn) {
        this.inSpawn = inSpawn;
    }

    public void setInArena(boolean inArena) {
        this.inArena = inArena;
    }

    public void setInKitEditor(boolean inKitEditor) {
        this.inKitEditor = inKitEditor;
    }

    public void setSearchingLadder(Ladder searchingLadder) {
        this.searchingLadder = searchingLadder;
    }

    public void setSearchingRange(int searchingRange) {
        this.searchingRange = searchingRange;
    }

    public void setQueue(BukkitTask queue) {
        this.queue = queue;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public void setDuel(Duel duel) {
        this.duel = duel;
    }

    public void setDuelInventory(Inventory duelInventory) {
        this.duelInventory = duelInventory;
    }

    public void setDuelPlayer(Player duelPlayer) {
        this.duelPlayer = duelPlayer;
    }

    public void setDuelRequests(Map<Player, Ladder> duelRequests) {
        this.duelRequests = duelRequests;
    }

    public void setEditingLadder(Ladder editingLadder) {
        this.editingLadder = editingLadder;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void setCurrentKit(Kit currentKit) {
        this.currentKit = currentKit;
    }

    public void setProjectiles(Set<Entity> projectiles) {
        this.projectiles = projectiles;
    }

    public void setDrops(Set<Entity> drops) {
        this.drops = drops;
    }

    public void setDamaged(Set<Player> damaged) {
        this.damaged = damaged;
    }

	public Map<Ladder, Integer> getRankedWins() {
		return rankedWins;
	}

	public void setRankedWins(Map<Ladder, Integer> rankedWins) {
		this.rankedWins = rankedWins;
        for (Ladder ladder : Ladder.getLadders()) {
            if (rankedWins.containsKey(ladder)) continue;
            rankedWins.put(ladder, 0);
        }
	}

	public Map<Ladder, Integer> getRankedLosses() {
		return rankedLosses;
	}

	public void setRankedLosses(Map<Ladder, Integer> rankedLosses) {
		this.rankedLosses = rankedLosses;
        for (Ladder ladder : Ladder.getLadders()) {
            if (rankedLosses.containsKey(ladder)) continue;
            rankedLosses.put(ladder, 0);
        }
	}

	public Map<Ladder, Integer> getUnRankedWins() {
		return unRankedWins;
	}

	public void setUnRankedWins(Map<Ladder, Integer> unRankedWins) {
		this.unRankedWins = unRankedWins;
        for (Ladder ladder : Ladder.getLadders()) {
            if (unRankedWins.containsKey(ladder)) continue;
            unRankedWins.put(ladder, 0);
        }
	}

	public Map<Ladder, Integer> getUnRankedLosses() {
		return unRankedLosses;
	}

	public void setUnRankedLosses(Map<Ladder, Integer> unRankedLosses) {
		this.unRankedLosses = unRankedLosses;
        for (Ladder ladder : Ladder.getLadders()) {
            if (unRankedLosses.containsKey(ladder)) continue;
            unRankedLosses.put(ladder, 0);
        }
	}

	public double getGlobalElo() {
		return globalElo;
	}

	public void setGlobalElo(double globalElo) {
		this.globalElo = globalElo;
	}

	public int getTotalMatches() {
		return totalMatches;
	}

	public void setTotalMatches(int totalMatches) {
		this.totalMatches = totalMatches;
	}

	public boolean isShowPlayers() {
		return showPlayers;
	}

	public void setShowPlayers(boolean showPlayers) {
		this.showPlayers = showPlayers;
	}

	public boolean isInvulnerability() {
		return invulnerability;
	}

	public void setInvulnerability(boolean invulnerability) {
		this.invulnerability = invulnerability;
	}

	public List<Player> getSpectatingPlayers() {
		return this.spectatingPlayers;
	}

	public void setSpectatingPlayers(List<Player> spectatingPlayers) {
		this.spectatingPlayers = spectatingPlayers;
	}

	public boolean isInSpectator() {
		return inSpectator;
	}

	public void setInSpectator(boolean inSpectator) {
		this.inSpectator = inSpectator;
	}

	public Player getSpectatingPlayer() {
		return spectating;
	}

	public void setSpectating(Player spectating) {
		this.spectating = spectating;
	}
    
	public void addSpectator(Player player) {
		this.spectatingPlayers.add(player);
	}
	
	public void removeSpectator(Player player) {
		if (this.spectatingPlayers.contains(player)) {
			this.spectatingPlayers.remove(player);
		}
	}

	public boolean isQueueCooldown() {
		return queueCooldown;
	}

	public void setQueueCooldown(boolean queueCooldown) {
		this.queueCooldown = queueCooldown;
	}
	
	public void clearSpectators() {
		for (Player p : this.getSpectatingPlayers()) {
			Nanny.getInstance().getProfileManager().sendToSpawn(p);
			p.sendMessage(ChatColor.RED + "The player you were spectating is no longer online!");
		}
		this.spectatingPlayers.clear();
	}
	
	public Player getSpectating() {
		return spectating;
	}

	public Ladder getSelected() {
		return selected;
	}

	public void setSelected(Ladder selected) {
		this.selected = selected;
	}

	public Arena getArena() {
		return arena;
	}

	public void setArena(Arena arena) {
		this.arena = arena;
	}
	
}

