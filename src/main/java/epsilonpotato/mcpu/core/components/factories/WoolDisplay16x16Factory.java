package epsilonpotato.mcpu.core.components.factories;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.BlockPlacingContext;
import epsilonpotato.mcpu.core.ComponentFactory;
import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.core.MCPUCore;
import epsilonpotato.mcpu.core.components.WoolDisplay16x16;
import epsilonpotato.mcpu.util.Triplet;

/**
 * An factory to create 16x16 Wool displays
 * @author Unknown6656
 */
public final class WoolDisplay16x16Factory extends ComponentFactory<WoolDisplay16x16>
{
    /**
     * @see epsilonpotato.mcpu.core.ComponentFactory#spawnComponent(epsilonpotato.mcpu.core.BlockPlacingContext, epsilonpotato.mcpu.core.MCPUCore, org.bukkit.entity.Player, int, int, int, epsilonpotato.mcpu.core.ComponentOrientation, int)
     */
    @Override
    public WoolDisplay16x16 spawnComponent(BlockPlacingContext context, MCPUCore caller, Player p, int x, int y, int z, ComponentOrientation or, int iocount) throws InvalidOrientationException
    {
        // TODO : display orientation

        createBase(context, x - 1, y - 1, z - 1, 20, 20);
        createWoolFrame(context, x + 1, y, z, 18, 1, 18);

        WoolDisplay16x16 wdisp = new WoolDisplay16x16(p, context.getWorld(), x, y, z);
        
        // CREATE PINS
        for (int i = 0; i < wdisp.getIOCount(); ++i)
        {
            Location loc = wdisp.getIOLocation(i);
            
            context.addBlock(loc, Material.IRON_BLOCK);
            
            if (i < 8)
                context.addBlock(loc.getBlockX() - 1, loc.getBlockY(), loc.getBlockZ(), Material.REDSTONE_WIRE);
            else
                context.addBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() + 1, Material.REDSTONE_WIRE);
        }
        
        return wdisp;
    }

    /**
     * @see epsilonpotato.mcpu.core.ComponentFactory#getEstimatedSize(epsilonpotato.mcpu.core.ComponentOrientation)
     */
    @Override
    public Triplet<Integer, Integer, Integer> getEstimatedSize(ComponentOrientation or)
    {
        return new Triplet<>(19, 1, 19);
    }

    
    /**
     * @see epsilonpotato.mcpu.core.ComponentFactory#getCircuitType()
     */
    @Override
    protected Class<WoolDisplay16x16> getCircuitType()
    {
        return WoolDisplay16x16.class;
    }
}
