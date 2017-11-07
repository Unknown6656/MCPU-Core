package epsilonpotato.mcpu.core.components.factories;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.*;
import epsilonpotato.mcpu.core.components.AnalogDigitalConverter;
import epsilonpotato.mcpu.util.Triplet;

/**
 * A factory to create analog-to-digital-converters
 * @author Unknown6656
 */
public final class AnalogDigitalConverterFactory extends ComponentFactory<AnalogDigitalConverter> 
{
    /**
     * @see epsilonpotato.mcpu.core.ComponentFactory#spawnComponent(epsilonpotato.mcpu.core.BlockPlacingContext, epsilonpotato.mcpu.core.MCPUCore, org.bukkit.entity.Player, int, int, int, epsilonpotato.mcpu.core.ComponentOrientation, int)
     */
    @Override
    protected AnalogDigitalConverter spawnComponent(BlockPlacingContext context, MCPUCore caller, Player p, int x, int y, int z, ComponentOrientation or, int iocount) throws InvalidOrientationException
    {
        AnalogDigitalConverter adc = new AnalogDigitalConverter(p, new Location(context.getWorld(), x, y, z));
        
        createBase(context, x, y - 1, z, 5, 7);
        createWoolFrame(context, x + 1, y, z, 3, 1, 7);
        createPins(context, adc);
        
        context.addBlock(x + 2, y, z, Material.GOLD_BLOCK);
        
        return adc;
    }
    
    /**
     * @see epsilonpotato.mcpu.core.ComponentFactory#getEstimatedSize(epsilonpotato.mcpu.core.ComponentOrientation)
     */
    @Override
    public Triplet<Integer, Integer, Integer> getEstimatedSize(ComponentOrientation or)
    {
        return new Triplet<>(5, 1, 7);
    }

   
    /**
     * @see epsilonpotato.mcpu.core.ComponentFactory#getCircuitType()
     */
    @Override
    protected Class<AnalogDigitalConverter> getCircuitType()
    {
        return AnalogDigitalConverter.class;
    }
}
