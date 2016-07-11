package us.chiraq.practicepots.utils;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemStackSerializer
{
  @SuppressWarnings("deprecation")
public static String serializeItemStackArray(ItemStack[] array)
  {
    String serialization = array.length + ";";
    for (int i = 0; i < array.length; i++)
    {
      ItemStack is = array[i];
      if (is != null)
      {
        String serializedItemStack = new String();
        
        String isType = String.valueOf(is.getType().getId());
        serializedItemStack = serializedItemStack + "t@" + isType;
        if (is.getDurability() != 0)
        {
          String isDurability = String.valueOf(is.getDurability());
          serializedItemStack = serializedItemStack + ":d@" + isDurability;
        }
        if (is.getAmount() != 1)
        {
          String isAmount = String.valueOf(is.getAmount());
          serializedItemStack = serializedItemStack + ":a@" + isAmount;
        }
        Map<Enchantment, Integer> isEnch = is.getEnchantments();
        if (isEnch.size() > 0) {
          for (Map.Entry<Enchantment, Integer> ench : isEnch.entrySet()) {
            serializedItemStack = serializedItemStack + ":e@" + ((Enchantment)ench.getKey()).getId() + "@" + ench.getValue();
          }
        }
        serialization = serialization + i + "#" + serializedItemStack + ";";
      }
    }
    return serialization;
  }
  
  @SuppressWarnings("deprecation")
public static ItemStack[] deserializeItemStackArray(String invString)
  {
    String[] serializedBlocks = invString.split(";");
    String invInfo = serializedBlocks[0];
    ItemStack[] deserialization = new ItemStack[Integer.valueOf(invInfo).intValue()];
    for (int i = 1; i < serializedBlocks.length; i++)
    {
      String[] serializedBlock = serializedBlocks[i].split("#");
      int stackPosition = Integer.valueOf(serializedBlock[0]).intValue();
      if (stackPosition < deserialization.length)
      {
        ItemStack is = null;
        Boolean createdItemStack = Boolean.valueOf(false);
        
        String[] serializedItemStack = serializedBlock[1].split(":");
        for (String itemInfo : serializedItemStack)
        {
          String[] itemAttribute = itemInfo.split("@");
          if (itemAttribute[0].equals("t"))
          {
            is = new ItemStack(Material.getMaterial(Integer.valueOf(itemAttribute[1]).intValue()));
            createdItemStack = Boolean.valueOf(true);
          }
          else if ((itemAttribute[0].equals("d")) && (createdItemStack.booleanValue()))
          {
            is.setDurability(Short.valueOf(itemAttribute[1]).shortValue());
          }
          else if ((itemAttribute[0].equals("a")) && (createdItemStack.booleanValue()))
          {
            is.setAmount(Integer.valueOf(itemAttribute[1]).intValue());
          }
          else if ((itemAttribute[0].equals("e")) && (createdItemStack.booleanValue()))
          {
            is.addEnchantment(Enchantment.getById(Integer.valueOf(itemAttribute[1]).intValue()), Integer.valueOf(itemAttribute[2]).intValue());
          }
        }
        deserialization[stackPosition] = is;
      }
    }
    return deserialization;
  }
}
