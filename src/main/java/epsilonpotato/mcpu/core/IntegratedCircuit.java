package epsilonpotato.mcpu.core;

import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public abstract class IntegratedCircuit
{
    protected final int x, y, z, xsize, ysize, zsize;
    protected final ComponentOrientation orientation;
    protected final Player creator;
    protected final World world;
    protected IOPort[] io;


    protected abstract ComponentOrientation[] getValidOrientations();
    protected abstract void onTick();
    public abstract Location getIOLocation(int port);
    public abstract String getState();

    
    public IntegratedCircuit(Player creator, Location loc, Triplet<Integer, Integer, Integer> size, int iocount, ComponentOrientation orient)
            throws InvalidOrientationException
    {
        ComponentOrientation[] ors = getValidOrientations();
        boolean has = false;
        
        for (ComponentOrientation o : ors)
            if (o == orient)
            {
                has = true;
                
                break;
            }
        
        if (!has)
            throw new InvalidOrientationException(orient, ors);

        orientation = orient;
        xsize = size.x;
        ysize = size.y;
        zsize = size.z;
        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();
        world = loc.getWorld();
        this.creator = creator;

        io = new IOPort[iocount];
        
        for (int i = 0; i < iocount; ++i)
            io[i] = new IOPort(0, false);
    }

    public final int getIOCount()
    {
        return io.length;
    }

    private final <T> T checkPort(int port, Function<IOPort, T> callback)
    {
        return (port >= 0) && (port < getIOCount()) ? callback.apply(io[port]) : null;
    }

    public final byte getIOValue(int port)
    {
        return checkPort(port, p -> p.getDirection() ? p.getValue() : 0);
    }

    public final void setIOValue(int port, byte value)
    {
        checkPort(port, p ->
        {
            if (!p.getDirection())
                p.setValue(value);
            
            return null;
        });
    }

    public final boolean getIODirection(int port)
    {
        return checkPort(port, p -> p.getDirection());
    }

    protected final void setIODirection(int port, boolean direction)
    {
        checkPort(port, p ->
        {
            p.setDirection(direction);
            
            return null;
        });
    }

    public final Player getCreator()
    {
        return creator;
    }

    public final World getWorld()
    {
        return world;
    }

    public Location getLocation()
    {
        return new Location(world, x, y, z);
    }

    public final boolean testCollision (Location loc)
    {
        if (world.getName().equals(loc.getWorld().getName()))
        {
            int _x = loc.getBlockX(), _y = loc.getBlockY(), _z = loc.getBlockZ();

            return (_x >= x) && (_x < x + xsize) &&
                   (_y >= y) && (_y < y + xsize) &&
                   (_z >= z) && (_z < z + zsize);
        }
        else
            return false;
    }   
}
