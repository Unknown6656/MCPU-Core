package epsilonpotato.mcpu.util;

import net.minecraft.server.v1_12_R1.*;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;


public final class ItemNBTHelper
{
    private ItemNBTHelper()
    {
    }
    
    public static final void addItem(Item item, int amount, HashMap<String, String> nbtTag, String[] lore, String display, Player p)
    {
        try
        {
            // Item item = (Item)Items.class.getDeclaredField(material).get(Items.class);
            ItemStack stack = new ItemStack(item, amount);
            NBTTagCompound com = new NBTTagCompound();
            
            for (String var : nbtTag.keySet())
                com.setString(var, nbtTag.get(var));
            
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
    
    public static final String getTag(org.bukkit.inventory.ItemStack stack, String key)
    {
        ItemStack nmsstack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound com = nmsstack.getTag();
        
        if (com != null && com.hasKey(key))
            return com.getString(key);
        else
            return null;
    }
}
