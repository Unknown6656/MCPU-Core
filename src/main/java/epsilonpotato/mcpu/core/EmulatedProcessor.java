package epsilonpotato.mcpu.core;

import java.net.URI;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
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

    /**
     * Gets the processor's io port
     * @param port I/O port
     */
    public abstract byte getIO(int port);

    /**
     * Set's the processor's I/O port to the given value (only if `getIODirection(port) == false`)
     * @param port I/O port
     * @param value new I/O value in the range of [0..15]
     */
    public abstract void setIO(int port, byte value);

    public abstract Location getIOLocation(int port);
    
    public abstract void setIODirection(int port, boolean direction);
    
    public abstract boolean getIODirection(int port);
    
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

    public final Location getNorthWestGoldBlock()
    {
        return getLocation().add(1, 0, 1);
    }

    public final Location getLocation()
    {
        return new Location(world, x, y, z);
    }

    protected final void getSign(Consumer<Sign> f)
    {
        Block b = getNorthWestGoldBlock().add(0, 1, 0).getBlock();
        
        if (b != null)
        {
            Sign s = (Sign)b.getState();
            
            f.accept(s);
            s.update();
        }
    }
    
    public final Triplet<Integer, Integer, Integer> getSize()
    {
        return new Triplet<>(xsize, ysize, zsize);
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
