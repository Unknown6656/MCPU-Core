package epsilonpotato.mcpu.core;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;


public abstract class LeverAwareEmulatedProcessor extends EmulatedProcessor
{
    public abstract Location getLeverLocation();
    
    
    public LeverAwareEmulatedProcessor(Player p, Location l, Triplet<Integer, Integer, Integer> size, int iocount, ComponentOrientation orient)
            throws InvalidOrientationException
    {
        super(p, l, size, iocount, orient);
    }
    
    @Override
    public final void onTick()
    {
        if (canrun && isEnabled())
        {
            executeNextInstruction();

            ++ticks;
        }
    }

    public final boolean isEnabled()
    {
        Block lever = getLeverLocation().getBlock();
        
        return lever.getType() == Material.LEVER ? lever.isBlockPowered() || (lever.getBlockPower() != 0) : true;
    }
}
