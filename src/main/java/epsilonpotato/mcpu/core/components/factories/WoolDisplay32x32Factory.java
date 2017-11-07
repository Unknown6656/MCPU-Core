package epsilonpotato.mcpu.core.components.factories;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.*;
import epsilonpotato.mcpu.util.*;
import epsilonpotato.mcpu.core.components.WoolDisplay32x32;

/**
 * An factory to create 32x32 Wool displays
 * @author Unknown6656
 */
public final class WoolDisplay32x32Factory extends ComponentFactory<WoolDisplay32x32>
{
    /**
     * @see epsilonpotato.mcpu.core.ComponentFactory#spawnComponent(epsilonpotato.mcpu.core.BlockPlacingContext, epsilonpotato.mcpu.core.MCPUCore, org.bukkit.entity.Player, int, int, int, epsilonpotato.mcpu.core.ComponentOrientation, int)
     */
    @Override
    public WoolDisplay32x32 spawnComponent(BlockPlacingContext context, MCPUCore caller, Player p, int x, int y, int z, ComponentOrientation or, int iocount) throws InvalidOrientationException
    {
        // TODO : display orientation

        createBase(context, x - 1, y - 1, z, 36, 34);
        createWoolFrame(context, x + 1, y, z, 34, 1, 34);
        
        WoolDisplay32x32 wdisp = new WoolDisplay32x32(p, context.getWorld(), x, y, z);
        
        // CREATE PINS
        for (int i = 0; i < wdisp.getIOCount(); ++i)
        {
            Location loc = wdisp.getIOLocation(i);
            
            context.addBlock(loc, Material.IRON_BLOCK);
            context.addBlock(loc.getBlockX() - 1, loc.getBlockY(), loc.getBlockZ(), Material.REDSTONE_WIRE);
        }
        
        return wdisp;
    }

    /**
     * @see epsilonpotato.mcpu.core.ComponentFactory#getEstimatedSize(epsilonpotato.mcpu.core.ComponentOrientation)
     */
    @Override
    public Triplet<Integer, Integer, Integer> getEstimatedSize(ComponentOrientation or)
    {
        return new Triplet<>(35, 1, 34);
    }

   
    /**
     * @see epsilonpotato.mcpu.core.ComponentFactory#getCircuitType()
     */
    @Override
    protected Class<WoolDisplay32x32> getCircuitType()
    {
        return WoolDisplay32x32.class;
    }
}
