package epsilonpotato.mcpu.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.util.*;

public abstract class IntegratedCircuit implements Serializable
{
    private static final long serialVersionUID = -1592305793835093270L;
    protected transient ArrayList<Triplet<Integer, Integer, Integer>> assocblocks;
    protected int x, y, z, xsize, ysize, zsize;
    protected ComponentOrientation orientation;
    protected transient Player creator;
    protected transient World world;
    protected IOPort[] io;
    

    protected abstract void serializeComponentSpecific(final YamlConfiguration conf);
    protected abstract void deserializeComponentSpecific(final YamlConfiguration conf);
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

    public final void setIODirection(int port, boolean direction)
    {
        checkPort(port, p ->
        {
            p.setDirection(direction);
            
            return null;
        });
    }

    public final void setAssociatedBlocks(Iterable<Block> blocks)
    {
        assocblocks = new ArrayList<>();
        
        for (Block b : blocks)
        {
            Location loc = b.getLocation();

            assocblocks.add(new Triplet<>(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        }
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

    public final boolean testCollision(Location loc)
    {
        if (world.getName().equals(loc.getWorld().getName()))
        {
            int _x = loc.getBlockX(), _y = loc.getBlockY(), _z = loc.getBlockZ();

            if ((assocblocks != null) && (assocblocks.size() > 0))
            {
                for (Triplet<Integer, Integer, Integer> pos : assocblocks)
                    if ((pos.x == _x) && (pos.y == _y) && (pos.z == _z))
                        return true;
            }
            else
                return (_x >= x) && (_x < x + xsize) &&
                       (_y >= y) && (_y < y + ysize) &&
                       (_z >= z) && (_z < z + zsize);
        }

        return false;
    }   

    public boolean isEmulatedProcessor()
    {
        return this instanceof EmulatedProcessor;
    }
    
    public void serialize(final YamlConfiguration conf)
    {
        conf.set("orient", orientation);
        conf.set("world", world == null ? "" : world.getUID());
        conf.set("x", x);
        conf.set("y", y);
        conf.set("z", z);
        conf.set("xs", xsize);
        conf.set("ys", ysize);
        conf.set("zs", zsize);
        conf.set("creator", creator == null ? "" : creator.getUniqueId());
        conf.set("iocount", getIOCount());
        
        YamlConfiguration confIO = conf.getOrCreateSection("io");
        YamlConfiguration confBlocks = conf.getOrCreateSection("asscoblocks");
        
        for (int i = 0; i < getIOCount(); ++i)
        {
            confIO.set(i + ".value", io[i].getValue());
            confIO.set(i + ".direction", io[i].getDirection());
        }
        
        if (assocblocks != null)
            for (Triplet<Integer, Integer, Integer> block : assocblocks)
            {
                confBlocks.set();
            }
    }
    
    public void deserialize(final YamlConfiguration conf)
    {
        
    }
}
