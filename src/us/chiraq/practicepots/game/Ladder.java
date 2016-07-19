package us.chiraq.practicepots.game;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.game.Arena;
import us.chiraq.practicepots.game.kit.Kit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Ladder {
    private static List<Ladder> ladders = new ArrayList<Ladder>();
    private String name;
    private Kit kit;
    private List<Arena> arenas;
    private int defaultElo;
    private ItemStack itemStack;
    private File file;
    private YamlConfiguration configuration;
    private boolean ranked = true;
    private boolean unranked = true;
    private boolean premiumRanked = true;
    private List<Player> unrankedQueue;
    private List<Player> rankedQueue;
    private List<Player> premiumRankedQueue;
    private int currentRankedMatches;
    private int currentPremiumRankedMatches;
    private int currentUnRankedMatches;

    public Ladder(String name, int defaultElo, ItemStack itemStack) {
        this.name = name;
        this.defaultElo = defaultElo;
        this.itemStack = itemStack;
        this.arenas = new ArrayList<Arena>();
        this.unrankedQueue = new ArrayList<Player>();
        this.rankedQueue = new ArrayList<Player>();
        this.premiumRankedQueue = new ArrayList<Player>();
        this.currentRankedMatches = 0;
        this.currentPremiumRankedMatches = 0;
        this.currentUnRankedMatches = 0;
        Ladder.getLadders().add(this);
    }

    public Ladder(String name, int defaultElo, Material material) {
        this(name, defaultElo, new ItemStack(material));
    }

    public Ladder(String name, int defaultElo, Material material, int data) {
        this(name, defaultElo, material);
        this.itemStack.setDurability((short)data);
    }

    private void setupFile() {
        this.file = new File(Nanny.getInstance().getDataFolder() + File.separator + "ladders", this.name.toLowerCase().replace(" ", "") + ".yml");
        if (!this.file.getParentFile().exists()) {
            this.file.getParentFile().mkdir();
        }
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.configuration = YamlConfiguration.loadConfiguration((File)this.file);
    }

    public void save() throws IOException {
        this.setupFile();
        for (Object key : this.configuration.getKeys(false)) {
            this.configuration.set((String)key, (Object)null);
        }
        this.configuration.save(this.file);
        this.configuration.set("NAME", (Object)this.name);
        this.configuration.set("MATERIAL", (Object)this.itemStack.getType().name());
        this.configuration.set("DATA", (Object)this.itemStack.getDurability());
        this.configuration.set("DEFAULT_ELO", (Object)this.defaultElo);
        this.configuration.set("QUEUE.RANKED", (Object)this.ranked);
        this.configuration.set("QUEUE.UNRANKED", (Object)this.unranked);
        this.configuration.set("QUEUE.PREMIUMRANKED", (Object)this.premiumRanked);
        ArrayList<String> arenaList = new ArrayList<String>();
        for (Arena arena : this.getArenas()) {
            arenaList.add(arena.getId());
        }
        this.configuration.set("ARENAS", arenaList);
        if (this.kit != null) {
            this.configuration.set("KIT", (Object)Kit.serialize(this.kit));
        }
        this.configuration.save(this.file);
    }

    public static Ladder getLadder(String name) {
        for (Ladder ladder : Ladder.getLadders()) {
            if (!ladder.getName().replace(" ", "").equalsIgnoreCase(name)) continue;
            return ladder;
        }
        return null;
    }

    public static List<Ladder> getLadders() {
        return ladders;
    }

    public String getName() {
        return this.name;
    }

    public Kit getKit() {
        return this.kit;
    }

    public List<Arena> getArenas() {
        return this.arenas;
    }

    public int getDefaultElo() {
        return this.defaultElo;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public File getFile() {
        return this.file;
    }

    public YamlConfiguration getConfiguration() {
        return this.configuration;
    }

    public boolean isRanked() {
        return this.ranked;
    }

    public boolean isUnranked() {
        return this.unranked;
    }

    public boolean isPremiumRanked() {
    	return this.premiumRanked;
    }
    
    public List<Player> getUnrankedQueue() {
        return this.unrankedQueue;
    }

    public List<Player> getRankedQueue() {
        return this.rankedQueue;
    }

    public List<Player> getPremiumRankedQueue() {
		return premiumRankedQueue;
	}

	public void setName(String name) {
        this.name = name;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public void setArenas(List<Arena> arenas) {
        this.arenas = arenas;
    }

    public void setDefaultElo(int defaultElo) {
        this.defaultElo = defaultElo;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setConfiguration(YamlConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setRanked(boolean ranked) {
        this.ranked = ranked;
    }
    
    public void setPremiumRanked(boolean premiumRanked) {
    	this.premiumRanked = premiumRanked;
    }

    public void setUnranked(boolean unranked) {
        this.unranked = unranked;
    }

    public void setUnrankedQueue(List<Player> unrankedQueue) {
        this.unrankedQueue = unrankedQueue;
    }

    public void setRankedQueue(List<Player> rankedQueue) {
        this.rankedQueue = rankedQueue;
    }

	public int getCurrentRankedMatches() {
		return currentRankedMatches;
	}

	public void setCurrentRankedMatches(int currentRankedMatches) {
		this.currentRankedMatches = currentRankedMatches;
	}

	public int getCurrentUnRankedMatches() {
		return currentUnRankedMatches;
	}

	public void setCurrentUnRankedMatches(int currentUnRankedMatches) {
		this.currentUnRankedMatches = currentUnRankedMatches;
	}

	public void setPremiumRankedQueue(List<Player> premiumRankedQueue) {
		this.premiumRankedQueue = premiumRankedQueue;
	}

	public int getCurrentPremiumRankedMatches() {
		return currentPremiumRankedMatches;
	}

	public void setCurrentPremiumRankedMatches(int currentPremiumRankedMatches) {
		this.currentPremiumRankedMatches = currentPremiumRankedMatches;
	}
    
}

