package epsilonpotato.mcpu.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.util.*;

public abstract class IntegratedCircuit
{
    protected ArrayList<Triplet<Integer, Integer, Integer>> assocblocks;
    protected int x, y, z, xsize, ysize, zsize;
    protected ComponentOrientation orientation;
    protected Player creator;
    protected World world;
    protected IOPort[] io;
    

    protected abstract void serializeComponentSpecific(final BinaryWriter wr) throws IOException;
    protected abstract void deserializeComponentSpecific(final BinaryReader rd) throws IOException, ClassNotFoundException;
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
                       (_y >= y) && (_y < y + xsize) &&
                       (_z >= z) && (_z < z + zsize);
        }

        return false;
    }   

    public boolean isEmulatedProcessor()
    {
        return this instanceof EmulatedProcessor;
    }
    
    public byte[] serialize() throws IOException
    {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        BinaryWriter wr = new BinaryWriter(s);

        wr.write(x);
        wr.write(y);
        wr.write(z);
        wr.write(xsize);
        wr.write(ysize);
        wr.write(zsize);
        wr.write(orientation.getValue());
        
        if (assocblocks != null)
        {
            wr.write(assocblocks.size());
         
            for (Triplet<Integer, Integer, Integer> block : assocblocks)
            {
                wr.write(block.x);
                wr.write(block.y);
                wr.write(block.z);
            }
        }
        else
            wr.write(0);

        wr.write(io.length);
        
        for (IOPort iop : io)
            wr.write((byte)(iop.getValue() & (iop.getDirection() ? 0x80 : 0x00)));
        
        wr.write(creator.getUniqueId());
        wr.write(world.getUID());

        serializeComponentSpecific(wr);
        
        wr.flush();
        
        return s.toByteArray();
    }
    
    public void deserialize(byte[] arr) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream s = new ByteArrayInputStream(arr);
        BinaryReader rd = new BinaryReader(s);

        x = rd.readInt();
        y = rd.readInt();
        z = rd.readInt();
        xsize = rd.readInt();
        ysize = rd.readInt();
        zsize = rd.readInt();
        orientation = ComponentOrientation.fromValue(rd.readByte());
        
        if (assocblocks != null)
            assocblocks.clear();
        else
            assocblocks = new ArrayList<>();

        int len = rd.readInt();
        
        for (int i = 0; i < len; ++i)
        {
            int lx = rd.readInt();
            int ly = rd.readInt();
            int lz = rd.readInt();
            
            assocblocks.add(new Triplet<>(lx, ly, lz));
        }
        
        len = rd.readInt();
        io = new IOPort[len];
        
        for (int i = 0; i < len; ++i)
        {
            byte val = rd.readByte();
            
            io[i] = new IOPort(val & 0x0f, (val & 0x80) != 0);
        }

        UUID plr = rd.readUUID();
        UUID wrld = rd.readUUID();

        Entity ep = MCPUCore.srv.getEntity(plr);
        
        if (ep instanceof Player)
            creator = (Player)ep;
        
        world = MCPUCore.srv.getWorld(wrld);

        deserializeComponentSpecific(rd);
        
        s.close();
    }
}
