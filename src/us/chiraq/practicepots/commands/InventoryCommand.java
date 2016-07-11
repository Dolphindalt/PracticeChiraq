package us.chiraq.practicepots.commands;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.utils.InventorySave;

import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InventoryCommand
implements CommandExecutor {
    private Nanny main = Nanny.getInstance();
    private LangFile lf = this.main.getLangFile();

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.lf.getString("NOT_PLAYER"));
            return true;
        }
        if (args.length == 1) {
            Player player = (Player)sender;
            UUID uuid = UUID.fromString(args[0]);
            InventorySave inventorySave = InventorySave.getSave(uuid);
            if (inventorySave == null) {
                player.sendMessage(this.lf.getString("INVENTORY_NOT_FOUND"));
                return true;
            }
            inventorySave.showPlayer(player);
            return true;
        }
        return false;
    }
}

