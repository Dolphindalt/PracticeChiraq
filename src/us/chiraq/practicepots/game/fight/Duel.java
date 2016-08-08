package us.chiraq.practicepots.game.fight;

import java.util.ArrayList;
import java.util.List;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.ConfigFile;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Arena;
import us.chiraq.practicepots.game.Ladder;
import us.chiraq.practicepots.game.kit.Kit;
import us.chiraq.practicepots.profile.Profile;
import us.chiraq.practicepots.utils.Items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Duel {
    private Nanny main = Nanny.getInstance();
    private LangFile lf = this.main.getLangFile();
    private ConfigFile cf = this.main.getConfigFile();
    private Player player1;
    private Player player2;
    private Ladder ladder;
    private Arena arena;
    private Profile profile1;
    private Profile profile2;
    private boolean started;
    private boolean countdown;
    private int ranked;
    
    private List<Entity> activeEntities;
    
    //private PlayerScoreboard scoreboard1;
    //private PlayerScoreboard scoreboard2;
    private BukkitTask task;

    public Duel(Player player1, Player player2, Profile profile1, Profile profile2, Ladder ladder, Arena arena, int ranked) {
    	this.activeEntities = new ArrayList<Entity>();
        this.player1 = player1;
        this.player2 = player2;
        this.profile1 = profile1;
        this.profile2 = profile2;
        this.ladder = ladder;
        this.arena = arena;
        this.ranked = ranked;
        if (ranked == 0) {
            ladder.getRankedQueue().remove((Object)player1);
            ladder.getRankedQueue().remove((Object)player2);
            ladder.setCurrentRankedMatches(ladder.getCurrentRankedMatches() + 1);
        } else if (ranked == 2) {
        	ladder.getPremiumRankedQueue().remove((Object)player1);
        	ladder.getPremiumRankedQueue().remove((Object)player2);
        	ladder.setCurrentPremiumRankedMatches(ladder.getCurrentPremiumRankedMatches() + 1);
        } else {
            ladder.getUnrankedQueue().remove((Object)player1);
            ladder.getUnrankedQueue().remove((Object)player2);
            ladder.setCurrentUnRankedMatches(ladder.getCurrentUnRankedMatches() + 1);
        }
        profile1.setDuel(this);
        profile2.setDuel(this);
        profile1.setInSpawn(false);
        profile2.setInSpawn(false);
        profile1.setInArena(true);
        profile2.setInArena(true);
        this.start();
    }

    @SuppressWarnings("deprecation")
	private void start() {
        Inventory inventory;
        ItemStack kitItem;
        this.countdown = true;
        this.player1.teleport(this.arena.getSpawnLocations()[0]);
        this.player2.teleport(this.arena.getSpawnLocations()[1]);
        this.player1.getInventory().clear();
        this.player2.getInventory().clear();
        
        if (this.profile1.getKits(this.ladder).isEmpty()) {
            if (this.ladder.getKit() != null) {
                this.ladder.getKit().apply(this.player1);
            }
        } else {
            inventory = Bukkit.createInventory((InventoryHolder)this.player1, (int)(this.cf.getInt("QUEUE.ROWS") * 9), (String)this.lf.getString("KIT_EDITOR.LOAD_NAME"));
            for (Kit kit : this.profile1.getKits(this.ladder)) {
                kitItem = Items.builder().setMaterial(Material.ENCHANTED_BOOK).setName((Object)ChatColor.GOLD + kit.getName()).build();
                inventory.addItem(new ItemStack[]{kitItem});
            }
            inventory.setItem(8, Items.builder().setMaterial(Material.BOOK).setName((Object)ChatColor.YELLOW + "Default " + this.ladder.getName() + " Kit").build());
            this.player1.openInventory(inventory);
        }
        if (this.profile2.getKits(this.ladder).isEmpty()) {
            if (this.ladder.getKit() != null) {
                this.ladder.getKit().apply(this.player2);
            }
        } else {
            inventory = Bukkit.createInventory((InventoryHolder)this.player2, (int)(this.cf.getInt("QUEUE.ROWS") * 9), (String)this.lf.getString("KIT_EDITOR.LOAD_NAME"));
            for (Kit kit : this.profile2.getKits(this.ladder)) {
                kitItem = Items.builder().setMaterial(Material.ENCHANTED_BOOK).setName((Object)ChatColor.GOLD + kit.getName()).build();
                inventory.addItem(new ItemStack[]{kitItem});
            }
            inventory.setItem(8, Items.builder().setMaterial(Material.BOOK).setName((Object)ChatColor.YELLOW + "Default " + this.ladder.getName() + " Kit").build());
            this.player2.openInventory(inventory);
        }
        
        Nanny.getInstance().getProfileManager().updateStaffView();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online == Duel.this.player1 || online == Duel.this.player2) continue;
            Nanny.getInstance().getProfileManager().hidePlayer(online, Duel.this.player1);
            Nanny.getInstance().getProfileManager().hidePlayer(online, Duel.this.player2);
        }
        Nanny.getInstance().getProfileManager().showPlayer(player1, player2);
        Nanny.getInstance().getProfileManager().showPlayer(player2, player1);

    	for (Player p1 : profile1.getSpectatingPlayers()) {
            Nanny.getInstance().getProfileManager().showPlayer(player1, player2);
            Nanny.getInstance().getProfileManager().showPlayer(player2, player1);
    		p1.teleport(player1);
    	}
    	
    	for (Player p2 : profile2.getSpectatingPlayers()) {
            Nanny.getInstance().getProfileManager().showPlayer(player1, player2);
            Nanny.getInstance().getProfileManager().showPlayer(player2, player1);
    		p2.teleport(player2);
    	}
    	
        this.profile1.setInvulnerability(true);
        this.profile2.setInvulnerability(true);
        
        player1.setVelocity(null);
        player2.setVelocity(null);
        
        for (int i = 0; i < 6; ++i) {
            final int finalI = i;
            this.task = new BukkitRunnable(){

                public void run() {
                    int pitch = 1;
                    if (finalI == 5) {
                        pitch = 2;
                        Duel.this.profile1.setInvulnerability(false);
                        Duel.this.profile2.setInvulnerability(false);
                    }
                    Duel.this.player1.playSound(Duel.this.player1.getLocation(), Sound.NOTE_PLING, 1.0f, (float)pitch);
                    Duel.this.player2.playSound(Duel.this.player1.getLocation(), Sound.NOTE_PLING, 1.0f, (float)pitch);
                    Duel.this.countdown = false;
                    Duel.this.started = true;
                }
            }.runTaskLater((Plugin)this.main, (long)(20 * i));
        }
    }

    public Nanny getMain() {
        return this.main;
    }

    public LangFile getLf() {
        return this.lf;
    }

    public ConfigFile getCf() {
        return this.cf;
    }

    public Player getPlayer1() {
        return this.player1;
    }

    public Player getPlayer2() {
        return this.player2;
    }

    public Ladder getLadder() {
        return this.ladder;
    }

    public Arena getArena() {
        return this.arena;
    }

    public Profile getProfile1() {
        return this.profile1;
    }

    public Profile getProfile2() {
        return this.profile2;
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isCountdown() {
        return this.countdown;
    }

    public int getRanked() {
        return this.ranked;
    }

    /*public PlayerScoreboard getScoreboard1() {
        return this.scoreboard1;
    }

    public PlayerScoreboard getScoreboard2() {
        return this.scoreboard2;
    }*/

    public BukkitTask getTask() {
        return this.task;
    }

    public void setMain(Nanny main) {
        this.main = main;
    }

    public void setLf(LangFile lf) {
        this.lf = lf;
    }

    public void setCf(ConfigFile cf) {
        this.cf = cf;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public void setLadder(Ladder ladder) {
        this.ladder = ladder;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public void setProfile1(Profile profile1) {
        this.profile1 = profile1;
    }

    public void setProfile2(Profile profile2) {
        this.profile2 = profile2;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void setCountdown(boolean countdown) {
        this.countdown = countdown;
    }
    //Dolphindalt was here
    public void setRanked(int ranked) {
        this.ranked = ranked;
    }

    /*public void setScoreboard1(PlayerScoreboard scoreboard1) {
        this.scoreboard1 = scoreboard1;
    }

    public void setScoreboard2(PlayerScoreboard scoreboard2) {
        this.scoreboard2 = scoreboard2;
    }
    */

    public void setTask(BukkitTask task) {
        this.task = task;
    }

	public List<Entity> getActiveEntities() {
		return activeEntities;
	}

	public void setActiveEntities(List<Entity> activeEntities) {
		this.activeEntities = activeEntities;
	}

	public void addActiveEntity(Entity entity) {
		this.activeEntities.add(entity);
	}
	
	public void removeActiveEntity(Entity entity) {
		this.removeActiveEntity(entity);
	}
	
}

