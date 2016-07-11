package us.chiraq.practicepots.game.fight;

import com.alexandeh.glaedr.scoreboards.Entry;
import com.alexandeh.glaedr.scoreboards.PlayerScoreboard;

import java.util.ArrayList;
import java.util.List;

import mkremins.fanciful.FancyMessage;
import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.ConfigFile;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Arena;
import us.chiraq.practicepots.game.Ladder;
import us.chiraq.practicepots.game.Team;
import us.chiraq.practicepots.game.kit.Kit;
import us.chiraq.practicepots.profile.Profile;
import us.chiraq.practicepots.utils.InventorySave;
import us.chiraq.practicepots.utils.Items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TeamDuel {
    private Nanny main = Nanny.getInstance();
    private LangFile lf = this.main.getLangFile();
    private ConfigFile cf = this.main.getConfigFile();
    private Team team1;
    private Team team2;
    private int team1Left;
    private int team2Left;
    private Ladder ladder;
    private Arena arena;
    private boolean started;
    private boolean countdown;
    private BukkitTask task;

    public TeamDuel(Team team1, Team team2, Ladder ladder, Arena arena) {
        this.team1 = team1;
        this.team2 = team2;
        this.ladder = ladder;
        this.arena = arena;
        this.team1Left = team1.getMembers().size();
        this.team2Left = team2.getMembers().size();
        team1.setDuel(this);
        team2.setDuel(this);
        team1.setInFight(true);
        team2.setInFight(true);
        team1.getChallenges().clear();
        team2.getChallenges().clear();
        for (Player player : this.getAllPlayers()) {
            Profile profile = Profile.getProfile(player.getUniqueId());
            profile.setInSpawn(false);
            profile.setInArena(true);
        }
        this.start();
    }

    private void setupTeam(Team team, Location location) {
        for (Player player : team.getMembers()) {
            player.getInventory().clear();
            Profile profile = Profile.getProfile(player.getUniqueId());
            if (profile.getKits(this.ladder).isEmpty()) {
                if (this.ladder.getKit() != null) {
                    this.ladder.getKit().apply(player);
                }
            } else {
                Inventory inventory = Bukkit.createInventory((InventoryHolder)player, (int)(this.cf.getInt("QUEUE.ROWS") * 9), (String)this.lf.getString("KIT_EDITOR.LOAD_NAME"));
                for (Kit kit : profile.getKits(this.ladder)) {
                    ItemStack kitItem = Items.builder().setMaterial(Material.ENCHANTED_BOOK).setName((Object)ChatColor.GOLD + kit.getName()).build();
                    inventory.addItem(new ItemStack[]{kitItem});
                }
                inventory.setItem(8, Items.builder().setMaterial(Material.BOOK).setName((Object)ChatColor.YELLOW + "Default " + this.ladder.getName() + " Kit").build());
                player.openInventory(inventory);
            }
            player.teleport(location);
            for (Player team1PlayerHidden1 : team.getMembers()) {
                player.showPlayer(team1PlayerHidden1);
            }
            team.resetScoreboard(player);
        }
    }

    @SuppressWarnings("deprecation")
	private void showAll() {
        for (Player teamPlayer1 : this.team1.getMembers()) {
            for (Player teamPlayer2 : this.team2.getMembers()) {
                teamPlayer1.showPlayer(teamPlayer2);
            }
        }
        for (Player teamPlayer2 : this.team2.getMembers()) {
            for (Player teamPlayer12 : this.team1.getMembers()) {
                teamPlayer2.showPlayer(teamPlayer12);
            }
        }
        for (Player player : this.getAllPlayers()) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (this.getAllPlayers().contains((Object)online)) continue;
                Nanny.getInstance().getProfileManager().hidePlayer(online, player);
            }
        }
    }

    private void setupScoreboard() {
        for (PlayerScoreboard scoreboard2 : this.team1.getScoreboards()) {
            for (String information : this.lf.getStringList("SCOREBOARD.TEAM_FIGHT_INFORMATION")) {
                if (information.contains("Duration") || information.contains("Warm")) {
                    if (information.contains("Duration")) {
                        new Entry(information, scoreboard2).setText(information).setCountup(true).setTime(0.0).send();
                        continue;
                    }
                    new Entry(information, scoreboard2).setText(information).setCountdown(true).setTime(5.0).send();
                    continue;
                }
                new Entry(information, scoreboard2).setText(information.replace("%FRIENDLYCOUNT%", "" + this.team1Left + "").replace("%ENEMYCOUNT%", "" + this.team2Left + "").replace("%LADDER%", this.ladder.getName())).send();
            }
        }
        for (PlayerScoreboard scoreboard2 : this.team2.getScoreboards()) {
            for (String information : this.lf.getStringList("SCOREBOARD.TEAM_FIGHT_INFORMATION")) {
                if (information.contains("Duration") || information.contains("Warm")) {
                    if (information.contains("Duration")) {
                        new Entry(information, scoreboard2).setText(information).setCountup(true).setTime(0.0).send();
                        continue;
                    }
                    new Entry(information, scoreboard2).setText(information).setCountdown(true).setTime(5.0).send();
                    continue;
                }
                new Entry(information, scoreboard2).setText(information.replace("%FRIENDLYCOUNT%", "" + this.team2Left + "").replace("%ENEMYCOUNT%", "" + this.team1Left + "").replace("%LADDER%", this.ladder.getName())).send();
            }
        }
    }

    public void setWinner(final Team team) {
        this.team1.setInFight(false);
        this.team2.setInFight(false);
        this.team1.setDuel(null);
        this.team2.setDuel(null);
        FancyMessage fancyMessage = new FancyMessage(this.lf.getString("TEAM.FINISH.INVENTORY_VIEW"));
        for (int i = 1; i < this.getAllPlayers().size() + 1; ++i) {
            Player player = this.getAllPlayers().get(i - 1);
            player.sendMessage(this.lf.getString("TEAM.FINISH.WINNER").replace("%WINNER%", team.getLeader().getName()));
            team.resetScoreboard(player);
            if (i == this.getAllPlayers().size()) {
                fancyMessage.then((Object)ChatColor.YELLOW + player.getName()).command("/_ " + player.getUniqueId());
                continue;
            }
            fancyMessage.then((Object)ChatColor.YELLOW + player.getName() + ", ").command("/_ " + player.getUniqueId());
        }
        if (this.task != null) {
            this.task.cancel();
        }
        new BukkitRunnable(){

            public void run() {
                for (Player player : team.getMembers()) {
                    Profile profile = Profile.getProfile(player.getUniqueId());
                    if (profile.isInSpawn()) continue;
                    new InventorySave(player);
                    TeamDuel.this.main.getProfileManager().sendToSpawn(player);
                }
            }
        }.runTaskLater((Plugin)this.main, 20);
    }

    private void start() {
        this.countdown = true;
        this.setupTeam(this.team1, this.arena.getSpawnLocations()[0]);
        this.setupTeam(this.team2, this.arena.getSpawnLocations()[1]);
        this.setupScoreboard();
        for (int i = 0; i < 6; ++i) {
            final int finalI = i;
            new BukkitRunnable(){

                public void run() {
                    int pitch = 1;
                    if (finalI == 5) {
                        pitch = 2;
                        TeamDuel.this.showAll();
                        Nanny.getInstance().getProfileManager().updateStaffView();
                    }
                    for (Player player : TeamDuel.this.getAllPlayers()) {
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, (float)pitch);
                    }
                    TeamDuel.this.countdown = false;
                    TeamDuel.this.started = true;
                }
            }.runTaskLater((Plugin)this.main, (long)(20 * i));
            this.task = new BukkitRunnable(){

                public void run() {
                    int amount;
                    Profile profile;
                    for (PlayerScoreboard scoreboard2 : TeamDuel.this.team1.getScoreboards()) {
                        profile = Profile.getProfile(scoreboard2.getPlayer().getUniqueId());
                        if (profile.isInSpawn()) continue;
                        for (Entry entry : scoreboard2.getEntries()) {
                            if (entry.getId().contains("Enemies alive")) {
                                amount = Integer.parseInt(ChatColor.stripColor((String)entry.getText().substring(entry.getText().lastIndexOf(" ", entry.getText().length()))).replace(" ", ""));
                                if (amount == TeamDuel.this.team2Left) continue;
                                entry.setText(entry.getText().substring(0, entry.getText().length() - 1) + TeamDuel.this.team2Left).send();
                                continue;
                            }
                            if (!entry.getId().contains("Friendlies") || (amount = Integer.parseInt(ChatColor.stripColor((String)entry.getText().substring(entry.getText().lastIndexOf(" ", entry.getText().length()))).replace(" ", ""))) == TeamDuel.this.team1Left) continue;
                            entry.setText(entry.getText().substring(0, entry.getText().length() - 1) + TeamDuel.this.team1Left).send();
                        }
                    }
                    for (PlayerScoreboard scoreboard2 : TeamDuel.this.team2.getScoreboards()) {
                        profile = Profile.getProfile(scoreboard2.getPlayer().getUniqueId());
                        if (profile.isInSpawn()) continue;
                        for (Entry entry : scoreboard2.getEntries()) {
                            if (entry.getId().contains("Enemies alive")) {
                                amount = Integer.parseInt(ChatColor.stripColor((String)entry.getText().substring(entry.getText().lastIndexOf(" ", entry.getText().length()))).replace(" ", ""));
                                if (amount == TeamDuel.this.team1Left) continue;
                                entry.setText(entry.getText().substring(0, entry.getText().length() - 1) + TeamDuel.this.team1Left).send();
                                continue;
                            }
                            if (!entry.getId().contains("Friendlies") || (amount = Integer.parseInt(ChatColor.stripColor((String)entry.getText().substring(entry.getText().lastIndexOf(" ", entry.getText().length()))).replace(" ", ""))) == TeamDuel.this.team2Left) continue;
                            entry.setText(entry.getText().substring(0, entry.getText().length() - 1) + TeamDuel.this.team2Left).send();
                        }
                    }
                }
            }.runTaskTimer((Plugin)this.main, 2, 2);
        }
    }

    public List<Player> getAllPlayers() {
        ArrayList<Player> players = new ArrayList<Player>();
        players.addAll(this.team1.getMembers());
        players.addAll(this.team2.getMembers());
        return players;
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

    public Team getTeam1() {
        return this.team1;
    }

    public Team getTeam2() {
        return this.team2;
    }

    public int getTeam1Left() {
        return this.team1Left;
    }

    public int getTeam2Left() {
        return this.team2Left;
    }

    public Ladder getLadder() {
        return this.ladder;
    }

    public Arena getArena() {
        return this.arena;
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isCountdown() {
        return this.countdown;
    }

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

    public void setTeam1(Team team1) {
        this.team1 = team1;
    }

    public void setTeam2(Team team2) {
        this.team2 = team2;
    }

    public void setTeam1Left(int team1Left) {
        this.team1Left = team1Left;
    }

    public void setTeam2Left(int team2Left) {
        this.team2Left = team2Left;
    }

    public void setLadder(Ladder ladder) {
        this.ladder = ladder;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void setCountdown(boolean countdown) {
        this.countdown = countdown;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

}

