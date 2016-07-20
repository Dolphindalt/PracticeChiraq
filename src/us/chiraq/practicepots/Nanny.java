package us.chiraq.practicepots;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import us.chiraq.practicepots.commands.ArenaCommand;
import us.chiraq.practicepots.commands.DuelCommand;
import us.chiraq.practicepots.commands.InventoryCommand;
import us.chiraq.practicepots.commands.LadderCommand;
import us.chiraq.practicepots.commands.LeaderboardCommands;
import us.chiraq.practicepots.commands.SaintCommand;
import us.chiraq.practicepots.commands.SettingsCommand;
import us.chiraq.practicepots.commands.SpectatorCommands;
import us.chiraq.practicepots.commands.TeamCommand;
import us.chiraq.practicepots.files.types.ConfigFile;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Ladder;
import us.chiraq.practicepots.listeners.DuelListeners;
import us.chiraq.practicepots.listeners.EditorListeners;
import us.chiraq.practicepots.listeners.QueueListeners;
import us.chiraq.practicepots.listeners.SpawnListeners;
import us.chiraq.practicepots.profile.Profile;
import us.chiraq.practicepots.profile.ProfileManager;
import us.chiraq.practicepots.utils.Data;
import us.chiraq.practicepots.utils.LocationSerialization;
import us.chiraq.practicepots.utils.packets.WrapperPlayServerNamedSoundEffect;
import us.chiraq.practicepots.utils.packets.WrapperPlayServerWorldEvent;

import java.io.IOException;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/*
 *   RENAMED THIS CLASS
 * AFTER THE ONE AND ONLY!
 *       KEEFNANNY
 */  

public class Nanny
extends JavaPlugin {
    private static Nanny instance;
    private ConfigFile configFile;
    private LangFile langFile;
    private ProfileManager profileManager;
    //private Glaedr glaedr;
    private ProtocolManager protocolManager;
    private Location kitEditor;
    private MongoClient mc;
    private DB db;
    private DBCollection collection;
    private DBCollection settings;

    public void onEnable() {
        instance = this;
        this.configFile = new ConfigFile();
        this.langFile = new LangFile();
        //this.glaedr = new Glaedr(this, this.langFile.getString("SCOREBOARD.TITLE"));
        
        //Scoreboard Lines (Start)
        //this.glaedr.getTopWrappers().add("&7&m----------------------");
        //this.glaedr.getBottomWrappers().add("&7&m----------------------");
        //Scoreboard Lines (End)
        
        if (this.configFile.getConfiguration().contains("KIT_EDITOR")) {
            this.kitEditor = LocationSerialization.deserializeLocation(this.configFile.getString("KIT_EDITOR"));
        }
        //this.glaedr.registerPlayers();
        this.setupDatabase();
        Data.loadArenas();
        Data.loadLadders();
        Data.loadProfiles();
        Data.loadSettings();
        this.registerManagers();
        this.registerListeners();
        this.registerCommands();
        this.registerTasks();
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.protocolManager.addPacketListener((PacketListener)new PacketAdapter((Plugin)this, new PacketType[]{WrapperPlayServerWorldEvent.TYPE}){

            public void onPacketSending(PacketEvent event) {
                WrapperPlayServerWorldEvent packet = new WrapperPlayServerWorldEvent(event.getPacket());
                Player player = event.getPlayer();
                Profile profile = Profile.getProfile(player.getUniqueId());
                for (Entity entity : profile.getProjectiles()) {
                    Location location = new Location(player.getWorld(), (double)packet.getX(), (double)packet.getY(), (double)packet.getZ());
                    if (location.distance(entity.getLocation()) > 2.0) continue;
                    profile.getProjectiles().remove((Object)entity);
                    return;
                }
                event.setCancelled(true);
            }
        });
        this.protocolManager.addPacketListener((PacketListener)new PacketAdapter((Plugin)this, new PacketType[]{WrapperPlayServerNamedSoundEffect.TYPE}){

            public void onPacketSending(PacketEvent event) {
                WrapperPlayServerNamedSoundEffect packet = new WrapperPlayServerNamedSoundEffect(event.getPacket());
                Player player = event.getPlayer();
                Profile profile = Profile.getProfile(player.getUniqueId());
                if (packet.getSoundName().equalsIgnoreCase("random.bow")) {
                    for (Object entity : player.getNearbyEntities(3.0, 3.0, 3.0)) {
                        if (!profile.getProjectiles().contains(entity)) continue;
                        return;
                    }
                    event.setCancelled(true);
                }
                if (packet.getSoundName().equals("game.player.hurt")) {
                    Location soundLocation = packet.getEffectPosition(player.getWorld());
                    for (Entity nearby : player.getNearbyEntities(20.0, 20.0, 20.0)) {
                        if (!(nearby instanceof Player)) continue;
                        Player nearbyPlayer = (Player)nearby;
                        if (!profile.getDamaged().contains((Object)nearbyPlayer) || soundLocation.distance(nearbyPlayer.getLocation()) > 3.0) continue;
                        return;
                    }
                    event.setCancelled(true);
                }
            }
        });
        
        Bukkit.broadcastMessage(ChatColor.GREEN + "Powered by " + ChatColor.GREEN + "Vapor " + ChatColor.RED + "Spigot" + ChatColor.GREEN + ", the official jar of Chiraq.us!");
    }

    public void onDisable() {
        for (Ladder ladder : Ladder.getLadders()) {
            try {
                ladder.save();
                continue;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.getKitEditor() != null) {
            this.configFile.getConfiguration().set("KIT_EDITOR", (Object)LocationSerialization.serializeLocation(this.kitEditor));
            try {
                this.configFile.getConfiguration().save(this.configFile.getFile());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Data.saveArenas();
            Data.saveSettings();
            Data.saveProfiles();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            this.db.getMongo().close();
        }
        this.db.getMongo().close();
        this.mc.close();
    }

    private void registerCommands() {
        this.getCommand("ladder").setExecutor((CommandExecutor)new LadderCommand());
        this.getCommand("arena").setExecutor((CommandExecutor)new ArenaCommand());
        this.getCommand("duel").setExecutor((CommandExecutor)new DuelCommand());
        this.getCommand("duel").setTabCompleter((TabCompleter)new DuelCommand());
        this.getCommand("_").setExecutor((CommandExecutor)new InventoryCommand());
        //Command 'putty' is only used for setting kiteditor spawn.
        this.getCommand("practice").setExecutor((CommandExecutor)new SaintCommand()); 
        this.getCommand("team").setExecutor((CommandExecutor)new TeamCommand());
        this.getCommand("t").setExecutor((CommandExecutor)new TeamCommand());
        this.getCommand("team").setTabCompleter((TabCompleter)new TeamCommand());
        this.getCommand("t").setTabCompleter((TabCompleter)new TeamCommand());
        this.getCommand("leaderboard").setExecutor((CommandExecutor)new LeaderboardCommands());
        this.getCommand("settings").setExecutor((CommandExecutor)new SettingsCommand());
        this.getCommand("spectator").setExecutor((CommandExecutor)new SpectatorCommands(profileManager));
        this.getCommand("spectator").setTabCompleter((TabCompleter)new SpectatorCommands(profileManager));
    }

    private void registerManagers() {
        this.profileManager = new ProfileManager();
        this.profileManager.setupPlayers();
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents((Listener)new SpawnListeners(), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new QueueListeners(), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new DuelListeners(), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new EditorListeners(), (Plugin)this);
    }

    private void registerTasks() {

    }
    
    @SuppressWarnings("deprecation")
	private void setupDatabase() {
        ConfigFile configFile = this.getConfigFile();
		//this.db = MongoClient.connect(configFile.getString("DATABASE.HOST"), configFile.getString("DATABASE.NAME")));
		if (configFile.getBoolean("DATABASE.LOGIN.ENABLED")) {
			MongoCredential cred = MongoCredential.createCredential(configFile.getString("DATABASE.LOGIN.USERNAME"), configFile.getString("DATABASE.NAME"), configFile.getString("DATABASE.LOGIN.PASSWORD").toCharArray());
		    //this.db.authenticate(configFile.getString("DATABASE.LOGIN.USERNAME"), configFile.getString("DATABASE.LOGIN.PASSWORD").toCharArray());
			mc = new MongoClient(new ServerAddress(), Arrays.asList(cred));
			this.db = mc.getDB(configFile.getString("DATABASE.NAME"));
		} else {
			mc = new MongoClient(configFile.getString("DATABASE.HOST"));
			this.db = mc.getDB(configFile.getString("DATABASE.NAME"));
		}
		
        this.collection = this.db.getCollection("profiles");
        this.settings = this.db.getCollection("settings");
    }

    public static Nanny getInstance() {
        return instance;
    }

    public ConfigFile getConfigFile() {
        return this.configFile;
    }

    public LangFile getLangFile() {
        return this.langFile;
    }

    public ProfileManager getProfileManager() {
        return this.profileManager;
    }

    //public Glaedr getGlaedr() {
    //    return this.glaedr;
    //}

    public ProtocolManager getProtocolManager() {
        return this.protocolManager;
    }

    public Location getKitEditor() {
        return this.kitEditor;
    }

    public DB getDb() {
        return this.db;
    }

    public DBCollection getCollection() {
        return this.collection;
    }

    public void setKitEditor(Location kitEditor) {
        this.kitEditor = kitEditor;
    }

	public DBCollection getSettings() {
		return settings;
	}

}

