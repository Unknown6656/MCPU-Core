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
    protected final void executeNextInstruction()
    {
        if (canrun)
        {
            Block lever = getLeverLocation().getBlock();

            if (lever.getType() == Material.LEVER ? lever.isBlockPowered() || (lever.getBlockPower() != 0) : true)
            {
                executeNextInstruction();

                ++ticks;
            }
        }
    }
}
