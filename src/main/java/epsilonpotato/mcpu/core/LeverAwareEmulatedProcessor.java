package epsilonpotato.mcpu.core;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.util.Triplet;


/**
 * Represents an abstract emulated processor which is aware of an on/off-lever attached to it in order to enable or disable it
 * @author Unknown6656
 */
public abstract class LeverAwareEmulatedProcessor extends EmulatedProcessor
{
    private static final long serialVersionUID = -6654702571745427110L;

    
    /**
     * Returns the processor on/off-lever location
     * @return The processor's lever location
     */
    public abstract Location getLeverLocation();
    

    /**
     * Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     * @deprecated Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     */
    @Deprecated
    public LeverAwareEmulatedProcessor()
    {
         super();
    }
    
    /**
     * Creates a new instance
     * @param p The creator
     * @param l The compontent's location
     * @param size The component's size
     * @param iocount The component's I/O port count
     * @param orient The component's desired orientation
     * @throws InvalidOrientationException Thrown, if the component was placed along an invalid orientation
     */
    public LeverAwareEmulatedProcessor(Player p, Location l, Triplet<Integer, Integer, Integer> size, int iocount, ComponentOrientation orient)
            throws InvalidOrientationException
    {
        super(p, l, size, iocount, orient);
    }
    
    /**
     * @see epsilonpotato.mcpu.core.EmulatedProcessor#onTick()
     */
    @Override
    public final void onTick()
    {
        if (canrun && isEnabled())
        {
            executeNextInstruction();

            ++ticks;
        }
    }

    /**
     * Returns whether the current processor is enabled
     * @return Enabled state
     */
    public final boolean isEnabled()
    {
        Block lever = getLeverLocation().getBlock();
        
        return lever.getType() == Material.LEVER ? lever.isBlockPowered() || (lever.getBlockPower() != 0) : true;
    }
}
