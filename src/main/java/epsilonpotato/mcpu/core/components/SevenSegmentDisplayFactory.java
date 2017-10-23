package epsilonpotato.mcpu.core.components;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.ComponentFactory;
import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.core.MCPUCore;

import static epsilonpotato.mcpu.core.MCPUCore.*;


public final class SevenSegmentDisplayFactory extends ComponentFactory<SevenSegmentDisplay>
{
    @Override
    @SuppressWarnings("deprecation")
    public SevenSegmentDisplay spawnComponent(MCPUCore caller, Player p, World w, int x, int y, int z, ComponentOrientation or, int iocount)
            throws InvalidOrientationException
    {
        
        // TODO : display orientation
        
        
        // CREATE STONE BASE
        for (int i = 0; i < 9; ++i)
            for (int j = 0; j < 13; ++j)
                SetBlock(w, x + i, y - 1, z + j, Material.STONE);

        // CREATE WOOL FRAME
        for (int i = 0; i < 9; ++i)
            for (int j = 0; j < 11; ++j)
                SetBlock(w, x + i, y, z + j, Material.WOOL, b -> b.setData(DyeColor.BLACK.getWoolData())); // TODO: fix deprecated calls
        
        // CREATE PINS
        for (int i = 0; i < 9; i += 2)
        {
            SetBlock(w, x + i, y, z + 11, Material.IRON_BLOCK);
            SetBlock(w, x + i, y, z + 12, Material.REDSTONE_WIRE);
        }
        
        return new SevenSegmentDisplay(p, x, y, z);
    }
}
