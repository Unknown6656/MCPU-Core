package epsilonpotato.mcpu.core.components.factories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.BlockPlacingContext;
import epsilonpotato.mcpu.core.ComponentFactory;
import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.core.MCPUCore;
import epsilonpotato.mcpu.core.components.SevenSegmentDisplay;
import epsilonpotato.mcpu.util.Triplet;


/**
 * An factory to create seven segment displays
 * @author Unknown6656
 */
public final class SevenSegmentDisplayFactory extends ComponentFactory<SevenSegmentDisplay>
{
    /**
     * @see epsilonpotato.mcpu.core.ComponentFactory#spawnComponent(epsilonpotato.mcpu.core.BlockPlacingContext, epsilonpotato.mcpu.core.MCPUCore, org.bukkit.entity.Player, int, int, int, epsilonpotato.mcpu.core.ComponentOrientation, int)
     */
    @Override
    public SevenSegmentDisplay spawnComponent(BlockPlacingContext context, MCPUCore caller, Player p, int x, int y, int z, ComponentOrientation or, int iocount)
            throws InvalidOrientationException
    {
        // TODO : display orientation
        

        createBase(context, x, y - 1, z, 9, 13);
        createWoolFrame(context, x, y, z, 9, 1, 11);

        // CREATE PINS
        for (int i = 0; i < 9; i += 2)
        {
            context.addBlock(x + i, y, z + 11, Material.IRON_BLOCK);
            context.addBlock(x + i, y, z + 12, Material.REDSTONE_WIRE);
        }
        
        return new SevenSegmentDisplay(p, context.getWorld(), x, y, z);
    }

    /**
     * @see epsilonpotato.mcpu.core.ComponentFactory#getEstimatedSize(epsilonpotato.mcpu.core.ComponentOrientation)
     */
    @Override
    public Triplet<Integer, Integer, Integer> getEstimatedSize(ComponentOrientation or)
    {
        return new Triplet<>(9, 1, 12);
    }
    
    /**
     * @see epsilonpotato.mcpu.core.ComponentFactory#getCircuitType()
     */
    @Override
    protected Class<SevenSegmentDisplay> getCircuitType()
    {
        return SevenSegmentDisplay.class;
    }
}
