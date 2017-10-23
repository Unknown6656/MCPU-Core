package epsilonpotato.mcpu.core;

import org.bukkit.Location;
import org.bukkit.entity.Player;


public abstract class SquareEmulatedProcessor extends LeverAwareEmulatedProcessor
{
    public SquareEmulatedProcessor(Player p, Location l, int iosidecount)
            throws Exception
    {
        super(p, l, new Triplet<>(iosidecount * 2 + 1, 2, iosidecount * 2 + 1), iosidecount * 4, ComponentOrientation.NORTH);
    }
    
    @Override
    public final Location getLeverLocation()
    {
        return new Location(world, x + 2, y, z + 1);
    }

    @Override
    protected final ComponentOrientation[] getValidOrientations()
    {
        return new ComponentOrientation[] { ComponentOrientation.NORTH };
    }
}
