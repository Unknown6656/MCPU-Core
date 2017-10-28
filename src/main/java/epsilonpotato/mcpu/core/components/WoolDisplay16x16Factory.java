package epsilonpotato.mcpu.core.components;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.BlockPlacingContext;
import epsilonpotato.mcpu.core.ComponentFactory;
import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.core.MCPUCore;
import epsilonpotato.mcpu.util.Triplet;

public final class WoolDisplay16x16Factory extends ComponentFactory<WoolDisplay16x16>
{
    @Override
    @SuppressWarnings("deprecation")
    public WoolDisplay16x16 spawnComponent(BlockPlacingContext context, MCPUCore caller, Player p, int x, int y, int z, ComponentOrientation or, int iocount) throws InvalidOrientationException
    {
        // TODO : display orientation

        createBase(context, x - 1, y - 1, z - 1, 20, 20);

        // CREATE WOOL FRAME
        for (int i = 1; i < 19; ++i)
            for (int j = 0; j < 18; ++j)
                context.addBlock(x + i, y, z + j, Material.WOOL, b -> b.setData(DyeColor.BLACK.getWoolData())); // TODO: fix deprecated calls
        
        WoolDisplay16x16 wdisp = new WoolDisplay16x16(p, context.getWorld(), x, y, z);
        
        // CREATE PINS
        for (int i = 0; i < wdisp.getIOCount(); ++i)
        {
            Location loc = wdisp.getIOLocation(i);
            
            context.addBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), Material.IRON_BLOCK);
            
            if (i < 8)
                context.addBlock(loc.getBlockX() - 1, loc.getBlockY(), loc.getBlockZ(), Material.REDSTONE_WIRE);
            else
                context.addBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() + 1, Material.REDSTONE_WIRE);
        }
        
        return wdisp;
    }

    @Override
    public Triplet<Integer, Integer, Integer> getEstimatedSize(ComponentOrientation or)
    {
        return new Triplet<>(19, 1, 19);
    }
}
