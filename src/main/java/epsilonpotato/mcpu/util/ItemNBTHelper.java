package epsilonpotato.mcpu.util;

import net.minecraft.server.v1_12_R1.*;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;


/**
 * A class containing all NBT Item helper methods
 * @author Unknown6656
 */
public final class ItemNBTHelper
{
    private ItemNBTHelper()
    {
    }
    
    /**
     * Gives the player a new item stack with the given NBT tags
     * @param item Item
     * @param amount Item count
     * @param tags NBT tags
     * @param lore Lore (hover description) text lines
     * @param display Display name
     * @param p Target player
     */
    public static final void addItem(Item item, int amount, HashMap<String, String> tags, String[] lore, String display, Player p)
    {
        try
        {
            // Item item = (Item)Items.class.getDeclaredField(material).get(Items.class);
            ItemStack stack = new ItemStack(item, amount);
            NBTTagCompound com = new NBTTagCompound();
            
            for (String var : tags.keySet())
                com.setString(var, tags.get(var));
            
            stack.setTag(com);
            
            PlayerInventory inv = p.getInventory();
            org.bukkit.inventory.ItemStack bukkitstack = CraftItemStack.asBukkitCopy(stack);
            ItemMeta meta = bukkitstack.getItemMeta();

            meta.setDisplayName(display);
            meta.setLore(Arrays.asList(lore));
            
            bukkitstack.setItemMeta(meta);
            
            inv.addItem(bukkitstack);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the NBT tag's value associated with the given item stack
     * @param stack Item stack
     * @param key NBT tag name
     * @return NBT tag value
     */
    public static final String getTag(org.bukkit.inventory.ItemStack stack, String key)
    {
        ItemStack nmsstack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound com = nmsstack.getTag();
        
        if (com != null && com.hasKey(key))
            return com.getString(key);
        else
            return null;
    }

    /**
     * Returns all stored NBT tags of the given item stack
     * @param stack Item stack
     * @return NBT tag collection (key -> value)
     */
    public static final HashMap<String, String> getTags(org.bukkit.inventory.ItemStack stack)
    {
        ItemStack nmsstack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound com = nmsstack.getTag();
        HashMap<String, String> map = new HashMap<>();
        
        for (String key : com.c())
            map.put(key, com.getString(key));
        
        return map;
    }
}
