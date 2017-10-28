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

public final class WoolDisplay32x32Factory extends ComponentFactory<WoolDisplay32x32>
{
    @Override
    @SuppressWarnings("deprecation")
    public WoolDisplay32x32 spawnComponent(BlockPlacingContext context, MCPUCore caller, Player p, int x, int y, int z, ComponentOrientation or, int iocount) throws InvalidOrientationException
    {
        // TODO : display orientation

        createBase(context, x - 1, y - 1, z, 36, 34);

        // CREATE WOOL FRAME
        for (int i = 1; i < 35; ++i)
            for (int j = 0; j < 34; ++j)
                context.addBlock(x + i, y, z + j, Material.WOOL, b -> b.setData(DyeColor.BLACK.getWoolData())); // TODO: fix deprecated calls
        
        WoolDisplay32x32 wdisp = new WoolDisplay32x32(p, context.getWorld(), x, y, z);
        
        // CREATE PINS
        for (int i = 0; i < wdisp.getIOCount(); ++i)
        {
            Location loc = wdisp.getIOLocation(i);
            
            context.addBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), Material.IRON_BLOCK);
            context.addBlock(loc.getBlockX() - 1, loc.getBlockY(), loc.getBlockZ(), Material.REDSTONE_WIRE);
        }
        
        return wdisp;
    }

    @Override
    public Triplet<Integer, Integer, Integer> getEstimatedSize(ComponentOrientation or)
    {
        return new Triplet<>(35, 1, 34);
    }
}
