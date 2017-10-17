package epsilonpotato.mcpu.core;

import java.net.URI;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;


public abstract class EmulatedProcessor
{
    protected final int x, y, z, xsize, ysize, zsize;
    protected final Player creator;
    protected final World world;
    protected final int iocount;

    public EmulatedProcessorEvent<String> onError;
    
    
    public abstract void nextInstruction();

    public abstract void reset();

    public abstract void start();

    public abstract void stop();

    public abstract boolean load(URI source);

    public abstract byte getIO(int port);

    public abstract void setIO(int port, byte value);

    public abstract String getState();

    public abstract long getTicksElapsed();

    public abstract int getIOCount();

    public boolean testCollision(Location loc)
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

    public EmulatedProcessor(Player p, Location l, Triplet<Integer, Integer, Integer> size, int iocount)
    {
        creator = p;
        world = l.getWorld();
        x = l.getBlockX();
        y = l.getBlockY();
        z = l.getBlockZ();
        xsize = size.x;
        ysize = size.y;
        zsize = size.z;
        this.iocount = iocount;
    }
}
