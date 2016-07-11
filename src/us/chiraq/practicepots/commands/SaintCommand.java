package us.chiraq.practicepots.commands;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.LangFile;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SaintCommand
implements CommandExecutor {
    private Nanny main = Nanny.getInstance();
    private LangFile lf = this.main.getLangFile();

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length >= 1) {
            if (!sender.hasPermission("staff.admin")) {
                sender.sendMessage(this.lf.getString("NO_PERMISSION"));
                return true;
            }
            if (args[0].equals("setkiteditor") && sender instanceof Player) {
                this.main.setKitEditor(((Player)sender).getLocation());
                sender.sendMessage((Object)ChatColor.RED + "Kit editor location updated!");
                return true;
            }
        }
        sender.sendMessage((Object)ChatColor.RED + "You lack the correct permissions to use this command!");
        return true;
    }
}

