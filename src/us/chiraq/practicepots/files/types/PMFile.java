package us.chiraq.practicepots.files.types;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import us.chiraq.practicepots.files.BaseFile;

public class PMFile
	extends BaseFile {
	    
		public PMFile() {
	        File file = new File(this.getMain().getDataFolder(), "pm.yml");
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
