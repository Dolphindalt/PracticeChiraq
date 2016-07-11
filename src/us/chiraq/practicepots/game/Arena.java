package us.chiraq.practicepots.game;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.LangFile;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;

public class Arena {
    private static Set<Arena> arenas = new HashSet<Arena>();
    private Nanny main = Nanny.getInstance();
    private LangFile lf = this.main.getLangFile();
    private Location[] spawnLocations;
    private String id;

    public Arena(String id) {
        this.id = id.toUpperCase();
        this.spawnLocations = new Location[2];
        Arena.getArenas().add(this);
    }

    public static Arena getArena(String id) {
        for (Arena arena : Arena.getArenas()) {
            if (!arena.getId().equalsIgnoreCase(id)) continue;
            return arena;
        }
        return null;
    }

    public static Set<Arena> getArenas() {
        return arenas;
    }

    public Nanny getMain() {
        return this.main;
    }

    public LangFile getLf() {
        return this.lf;
    }

    public Location[] getSpawnLocations() {
        return this.spawnLocations;
    }

    public String getId() {
        return this.id;
    }

    public void setMain(Nanny main) {
        this.main = main;
    }

    public void setLf(LangFile lf) {
        this.lf = lf;
    }

    public void setSpawnLocations(Location[] spawnLocations) {
        this.spawnLocations = spawnLocations;
    }

    public void setId(String id) {
        this.id = id;
    }
}

