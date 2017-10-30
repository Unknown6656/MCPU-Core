package epsilonpotato.mcpu.core.components.factories;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.BlockPlacingContext;
import epsilonpotato.mcpu.core.ComponentFactory;
import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.core.MCPUCore;
import epsilonpotato.mcpu.core.components.Multiplexer1to8;
import epsilonpotato.mcpu.util.Triplet;

public final class Multiplexer1to8Factory extends ComponentFactory<Multiplexer1to8>
{
    @Override
    public Multiplexer1to8 spawnComponent(BlockPlacingContext context, MCPUCore caller, Player p, int x, int y, int z, ComponentOrientation or, int iocount) throws InvalidOrientationException
    {
        // TODO : display orientation
        

        createBase(context, x, y - 1, z, 7, 16);
        createWoolFrame(context, x + 1, y, z, 5, 1, 15);

        context.addBlock(x + 3, y, z, Material.GOLD_BLOCK);
        
        Multiplexer1to8 mux = new Multiplexer1to8(p, new Location(context.getWorld(), x, y, z), or);
        
        // CREATE PINS
        for (int i = 0; i < mux.getIOCount(); ++i)
            context.addBlock(mux.getIOLocation(i), Material.IRON_BLOCK);
        
        return mux;
    }
    
    public Triplet<Integer, Integer, Integer> getEstimatedSize(ComponentOrientation or)
    {
        return new Triplet<>(7, 1, 16);
    }
}
