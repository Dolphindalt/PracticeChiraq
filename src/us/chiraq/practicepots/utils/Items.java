package us.chiraq.practicepots.utils;

import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;

public final class Items {
    private Items() {
    }

    public static ItemStackBuilder builder() {
        return new ItemStackBuilder();
    }

    public static ItemStack createColoredArmor(ItemStack item, Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta)item.getItemMeta();
        meta.setColor(color);
        item.setItemMeta((ItemMeta)meta);
        return item;
    }

    public static ItemStackBuilder editor(ItemStack itemStack) {
        return new ItemStackBuilder(itemStack);
    }

    public static class ItemStackBuilder {
        private final ItemStack itemStack;

        ItemStackBuilder() {
            this.itemStack = new ItemStack(Material.QUARTZ);
        }

        public ItemStackBuilder(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public ItemStackBuilder setItemMeta(ItemMeta meta) {
            this.itemStack.setItemMeta(meta);
            return this;
        }

        public ItemStackBuilder setMaterial(Material material) {
            this.itemStack.setType(material);
            return this;
        }

        public ItemStackBuilder changeAmount(int change) {
            this.itemStack.setAmount(this.itemStack.getAmount() + change);
            return this;
        }

        public ItemStackBuilder setAmount(int amount) {
            this.itemStack.setAmount(amount);
            return this;
        }

        public ItemStackBuilder setData(short data) {
            this.itemStack.setDurability(data);
            return this;
        }

        public ItemStackBuilder setData(MaterialData data) {
            this.itemStack.setData(data);
            return this;
        }

        public ItemStackBuilder setEnchantments(HashMap<Enchantment, Integer> enchantments) {
            for (Enchantment enchantment : this.itemStack.getEnchantments().keySet()) {
                this.itemStack.removeEnchantment(enchantment);
            }
            this.itemStack.addUnsafeEnchantments(enchantments);
            return this;
        }

        public ItemStackBuilder addEnchantment(Enchantment enchantment, int level) {
            this.itemStack.addUnsafeEnchantment(enchantment, level);
            return this;
        }

        public ItemStackBuilder setName(String name) {
            ItemMeta itemMeta = this.itemStack.getItemMeta();
            itemMeta.setDisplayName(name);
            this.itemStack.setItemMeta(itemMeta);
            return this;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
		public /* varargs */ ItemStackBuilder setLore(String ... lore) {
            ItemMeta itemMeta = this.itemStack.getItemMeta();
            itemMeta.setLore((List)Lists.newArrayList((Object[])lore));
            this.itemStack.setItemMeta(itemMeta);
            return this;
        }

        public ItemStackBuilder setLore(List<String> lore) {
            ItemMeta itemMeta = this.itemStack.getItemMeta();
            itemMeta.setLore(lore);
            this.itemStack.setItemMeta(itemMeta);
            return this;
        }

        public ItemStack build() {
            return this.itemStack;
        }
    }

    public static enum Armor {
        HELMET(Material.LEATHER_HELMET),
        CHESTPLATE(Material.LEATHER_CHESTPLATE),
        LEGGINGS(Material.LEATHER_LEGGINGS),
        BOOTS(Material.LEATHER_BOOTS);
        
        private Material material;

        private Armor(Material material) {
            this.material = material;
        }

        public Material getMaterial() {
            return this.material;
        }
    }

    public static String addChatColor(String lore)
	{
		if(lore.contains("&0"))
		{
			lore = lore.replace("&0", "" + ChatColor.BLACK);
		}
		if(lore.contains("&1"))
		{
			lore = lore.replace("&1", "" + ChatColor.DARK_BLUE);
		}
		if(lore.contains("&2"))
		{
			lore = lore.replace("&2", "" + ChatColor.DARK_GREEN);
		}
		if(lore.contains("&3"))
		{
			lore = lore.replace("&3", "" + ChatColor.DARK_AQUA);
		}
		if(lore.contains("&4"))
		{
			lore = lore.replace("&4", "" + ChatColor.DARK_RED);
		}
		if(lore.contains("&5"))
		{
			lore = lore.replace("&5", "" + ChatColor.DARK_PURPLE);
		}
		if(lore.contains("&6"))
		{
			lore = lore.replace("&6", "" + ChatColor.GOLD);
		}
		if(lore.contains("&7"))
		{
			lore = lore.replace("&7", "" + ChatColor.GRAY);
		}
		if(lore.contains("&8"))
		{
			lore = lore.replace("&8", "" + ChatColor.DARK_GRAY);
		}
		if(lore.contains("&9"))
		{
			lore = lore.replace("&9", "" + ChatColor.BLUE);
		}
		if(lore.contains("&a"))
		{
			lore = lore.replace("&a", "" + ChatColor.GREEN);
		}
		if(lore.contains("&b"))
		{
			lore = lore.replace("&b", "" + ChatColor.AQUA);
		}
		if(lore.contains("&c"))
		{
			lore = lore.replace("&c", "" + ChatColor.RED);
		}
		if(lore.contains("&d"))
		{
			lore = lore.replace("&d", "" + ChatColor.LIGHT_PURPLE);
		}
		if(lore.contains("&e"))
		{
			lore = lore.replace("&e", "" + ChatColor.YELLOW);
		}
		if(lore.contains("&f"))
		{
			lore = lore.replace("&f", "" + ChatColor.WHITE);
		}
		if(lore.contains("&k"))
		{
			lore = lore.replace("&k", "" + ChatColor.MAGIC);
		}
		return lore;
	}
    
}

