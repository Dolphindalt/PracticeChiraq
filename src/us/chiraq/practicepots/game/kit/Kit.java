package us.chiraq.practicepots.game.kit;

import us.chiraq.practicepots.game.Ladder;
import us.chiraq.practicepots.utils.ItemStackSerializer;

import java.io.Serializable;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("serial")
public class Kit
implements Serializable {
    private ItemStack[] armor;
    private ItemStack[] inventory;
    private Ladder ladder;
    private String name;

    public Kit(Ladder ladder, ItemStack[] armor, ItemStack[] inventory) {
        this.ladder = ladder;
        this.armor = armor;
        this.inventory = inventory;
    }

    public void apply(Player player) {
        player.getInventory().setContents(this.inventory);
        player.getInventory().setArmorContents(this.armor);
    }

    public static String serialize(Kit kit) {
        return ItemStackSerializer.serializeItemStackArray(kit.getArmor()) + "|" + ItemStackSerializer.serializeItemStackArray(kit.getInventory());
    }

    public ItemStack[] getArmor() {
        return this.armor;
    }

    public ItemStack[] getInventory() {
        return this.inventory;
    }

    public Ladder getLadder() {
        return this.ladder;
    }

    public String getName() {
        return this.name;
    }

    public void setArmor(ItemStack[] armor) {
        this.armor = armor;
    }

    public void setInventory(ItemStack[] inventory) {
        this.inventory = inventory;
    }

    public void setLadder(Ladder ladder) {
        this.ladder = ladder;
    }

    public void setName(String name) {
        this.name = name;
    }
}

