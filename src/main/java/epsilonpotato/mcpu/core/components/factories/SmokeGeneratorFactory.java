package epsilonpotato.mcpu.core.components.factories;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.BlockPlacingContext;
import epsilonpotato.mcpu.core.ComponentFactory;
import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.core.MCPUCore;
import epsilonpotato.mcpu.core.components.SmokeGenerator;
import epsilonpotato.mcpu.util.Triplet;

public final class SmokeGeneratorFactory extends ComponentFactory<SmokeGenerator>
{
    @Override
    public Triplet<Integer, Integer, Integer> getEstimatedSize(ComponentOrientation or)
    {
        return new Triplet<>(3, 1, 3);
    }
    
    @Override
    public SmokeGenerator spawnComponent(BlockPlacingContext context, MCPUCore caller, Player p, int x, int y, int z, ComponentOrientation or, int iocount) throws InvalidOrientationException
    {
        createWoolFrame(context, x, y, z, 3, 1, 3);

        SmokeGenerator gen = new SmokeGenerator(p, new Location(context.getWorld(), x, y, z), or);        
        
        context.addBlock(x + 1, y, z + 1, Material.MAGMA);
        context.addBlock(gen.getIOLocation(0), Material.IRON_BLOCK);
        
        return gen;
    }
}
