package epsilonpotato.mcpu.core;

import org.bukkit.Location;
import org.bukkit.entity.Player;


public abstract class SquareEmulatedProcessor extends LeverAwareEmulatedProcessor
{
    protected final int sidecount; 
    
    
    public SquareEmulatedProcessor(Player p, Location l, int iosidecount)
            throws Exception
    {
        super(p, l, new Triplet<>(iosidecount * 2 + 1, 2, iosidecount * 2 + 1), iosidecount * 4, ComponentOrientation.NORTH);
        
        sidecount = iosidecount;
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

    @Override
    public final Location getIOLocation(int port)
    {
        int x = this.x + 1;
        int z = this.z + 1;
        
        if (port < sidecount)
        {
            x += 2 * port;
            z -= 1;
        }
        else if ((port -= sidecount) < sidecount)
        {
            x += sidecount * 2 - 1;
            z += 2 * port;
        }
        else if ((port -= sidecount) < sidecount)
        {
            x += 2 * (sidecount - port - 1);
            z += sidecount * 2 - 1;
        }
        else
        {
            port -= sidecount;
            
            x -= 1;
            z += 2 * (sidecount - port - 1);
        }
        
        return new Location(world, x, this.y, z);
    }
}
