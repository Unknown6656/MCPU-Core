package epsilonpotato.mcpu.core.components;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.BlockPlacingContext;
import epsilonpotato.mcpu.core.ComponentFactory;
import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.core.MCPUCore;
import epsilonpotato.mcpu.util.Triplet;


public final class SevenSegmentDisplayFactory extends ComponentFactory<SevenSegmentDisplay>
{
    @Override
    @SuppressWarnings("deprecation")
    public SevenSegmentDisplay spawnComponent(BlockPlacingContext context, MCPUCore caller, Player p, int x, int y, int z, ComponentOrientation or, int iocount)
            throws InvalidOrientationException
    {
        // TODO : display orientation
        

        createBase(context, x, y - 1, z, 9, 13);

        // CREATE WOOL FRAME
        for (int i = 0; i < 9; ++i)
            for (int j = 0; j < 11; ++j)
                context.addBlock(x + i, y, z + j, Material.WOOL, b -> b.setData(DyeColor.BLACK.getWoolData())); // TODO: fix deprecated calls
        
        // CREATE PINS
        for (int i = 0; i < 9; i += 2)
        {
            context.addBlock(x + i, y, z + 11, Material.IRON_BLOCK);
            context.addBlock(x + i, y, z + 12, Material.REDSTONE_WIRE);
        }
        
        return new SevenSegmentDisplay(p, context.getWorld(), x, y, z);
    }

    @Override
    public Triplet<Integer, Integer, Integer> getEstimatedSize(ComponentOrientation or)
    {
        return new Triplet<>(9, 1, 12);
    }
}
