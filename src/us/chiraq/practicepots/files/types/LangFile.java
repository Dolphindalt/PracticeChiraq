package us.chiraq.practicepots.files.types;

import us.chiraq.practicepots.files.BaseFile;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class LangFile
extends BaseFile {
    public LangFile() {
        File file = new File(this.getMain().getDataFolder(), "lang.yml");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }
        this.getMain().saveResource(file.getName(), false);
        if (!file.exists()) {
            Bukkit.getConsoleSender().sendMessage((Object)ChatColor.GREEN + this.getMain().getName() + ": Successfully created " + file.getName() + "!");
        } else {
            Bukkit.getConsoleSender().sendMessage((Object)ChatColor.GREEN + this.getMain().getName() + ": Successfully loaded " + file.getName() + "!");
        }
        this.setFile(file);
        this.setConfiguration(YamlConfiguration.loadConfiguration((File)file));
    }
}

