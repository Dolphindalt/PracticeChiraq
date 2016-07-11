package us.chiraq.practicepots.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
public class InventorySave {
    private static Set<InventorySave> saves = new HashSet<InventorySave>();
    private Player player;
    private ItemStack[] contents;
    private ItemStack[] armor;
    private int hunger;
    private double health;
    private List<PotionEffect> effects;

    public InventorySave(Player player) {
        this.player = player;
        this.contents = player.getInventory().getContents();
        this.armor = player.getInventory().getArmorContents();
        this.health = ((CraftPlayer)player).getHealth();
        this.hunger = player.getFoodLevel();
        this.overwrite();
        InventorySave.getSaves().add(this);
    }

    private void overwrite() {
        InventorySave inventorySave = InventorySave.getSave(this.player.getUniqueId());
        if (inventorySave != null) {
            InventorySave.getSaves().remove(inventorySave);
        }
    }

    public void showPlayer(Player toShow) {
        int i;
        Inventory inventory = Bukkit.createInventory((InventoryHolder)toShow, (int)54, (String)this.player.getName());
        for (i = 0; i < this.contents.length; ++i) {
            inventory.setItem(i, this.contents[i]);
        }
        for (i = 0; i < this.armor.length; ++i) {
            inventory.setItem(36 + i, this.armor[i]);
        }
        toShow.openInventory(inventory);
    }

    public static InventorySave getSave(UUID uuid) {
        for (InventorySave inventorySave : InventorySave.getSaves()) {
            if (!inventorySave.getPlayer().getUniqueId().equals(uuid)) continue;
            return inventorySave;
        }
        return null;
    }

    public static Set<InventorySave> getSaves() {
        return saves;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ItemStack[] getContents() {
        return this.contents;
    }

    public ItemStack[] getArmor() {
        return this.armor;
    }

    public int getHunger() {
        return this.hunger;
    }

    public double getHealth() {
        return this.health;
    }

    public List<PotionEffect> getEffects() {
        return this.effects;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    public void setArmor(ItemStack[] armor) {
        this.armor = armor;
    }

    public void setHunger(int hunger) {
        this.hunger = hunger;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public void setEffects(List<PotionEffect> effects) {
        this.effects = effects;
    }
}

