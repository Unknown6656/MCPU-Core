package epsilonpotato.mcpu.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.util.*;


/**
 * Represents an abstract integrated circuit 
 * @author Unknown6656
 */
public abstract class IntegratedCircuit implements Serializable
{
    private static final long serialVersionUID = -1592305793835093270L;
    /**
     * A list of associated block coordinates inside the containing world
     */
    protected transient ArrayList<Triplet<Integer, Integer, Integer>> assocblocks;
    /**
     * The circuit's X-coordinates
     */
    protected int x;
    /**
     * The circuit's Y-coordinates
     */
    protected int y;
    /**
     * The circuit's Z-coordinates
     */
    protected int z;
    /**
     * The circuit's size in X-direction
     */
    protected int xsize;
    /**
     * The circuit's size in Y-direction
     */
    protected int ysize;
    /**
     * The circuit's size in Z-direction
     */
    protected int zsize;
    /**
     * The component's orientation
     */
    protected ComponentOrientation orientation;
    /**
     * The player which created the component
     */
    protected transient Player creator;
    /**
     * The world, into which the component was placed
     */
    protected transient World world;
    /**
     * The component's I/O ports
     */
    protected IOPort[] io;


    /**
     * Serialises component-specific data into the given YAML configuration
     * @param conf YAML configuration
     */
    protected abstract void serializeComponentSpecific(final YamlConfiguration conf);
    /**
     * Deserialises component-specific data from the given YAML configuration
     * @param conf YAML configuration
     */
    protected abstract void deserializeComponentSpecific(final YamlConfiguration conf);
    /**
     * Returns an array of valid component orientations
     * @return Array of valid component orientations
     */
    protected abstract ComponentOrientation[] getValidOrientations();
    /**
     * The components handler for world tick updates
     */
    protected abstract void onTick();
    /**
     * Returns the location of the given I/O port
     * @param port I/O port number
     * @return Port location
     */
    public abstract Location getIOLocation(int port);
    /**
     * Returns the current component's state as string
     * @return The current component's state
     */
    public abstract String getState();
    

    /**
     * Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     * @deprecated Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     */
    @Deprecated
    protected IntegratedCircuit()
    {
    }
     
    /**
     * Creates a new instance
     * @param creator The creator
     * @param loc The compontent's location
     * @param size The component's size
     * @param iocount The component's I/O port count
     * @param orient The component's desired orientation
     * @throws InvalidOrientationException Thrown, if the component was placed along an invalid orientation
     */
    protected IntegratedCircuit(Player creator, Location loc, Triplet<Integer, Integer, Integer> size, int iocount, ComponentOrientation orient)
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

    /**
     * Returns the number of I/O ports
     * @return I/O port count
     */
    public final int getIOCount()
    {
        return io.length;
    }

    private final <T> T checkPort(int port, Function<IOPort, T> callback)
    {
        return (port >= 0) && (port < getIOCount()) ? callback.apply(io[port]) : null;
    }

    /**
     * Returns the value of the given I/O port
     * @param port I/O port number (starting at 0)
     * @return The port's value (in the inclusive range of [0..15])
     */
    public final byte getIOValue(int port)
    {
        return checkPort(port, p -> p.getDirection() ? p.getValue() : 0);
    }

    /**
     * Sets the given I/O port's value to the new given one
     * @param port I/O port number (starting at 0)
     * @param value The new I/O port value (in the inclusive range of [0..15])
     */
    public final void setIOValue(int port, byte value)
    {
        checkPort(port, p ->
        {
            if (!p.getDirection())
                p.setValue(value);
            
            return null;
        });
    }

    /**
     * Returns the direction of the given I/O port
     * @param port I/O port number (starting at 0)
     * @return The port's direction (true := out, false := in)
     */
    public final boolean getIODirection(int port)
    {
        return checkPort(port, p -> p.getDirection());
    }

    /**
     * Sets the given I/O port's direction to the new given one
     * @param port I/O port number (starting at 0)
     * @param direction The port's direction (true := out, false := in)
     */
    public final void setIODirection(int port, boolean direction)
    {
        checkPort(port, p ->
        {
            p.setDirection(direction);
            
            return null;
        });
    }

    /**
     * Sets the component's associated blocks to the given ones
     * @param blocks List of blocks associated with the current component
     */
    public final void setAssociatedBlocks(Iterable<Block> blocks)
    {
        assocblocks = new ArrayList<>();
        
        for (Block b : blocks)
        {
            Location loc = b.getLocation();

            assocblocks.add(new Triplet<>(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        }
    }
    
    /**
     * Returns the player which has created the current component
     * @return The current component's creating player 
     */
    public final Player getCreator()
    {
        return creator;
    }

    /**
     * Returns the world into which the current component has been placed
     * @return The current component's world
     */
    public final World getWorld()
    {
        return world;
    }

    /**
     * Returns the current component's location
     * @return The component's location
     */
    public Location getLocation()
    {
        return new Location(world, x, y, z);
    }

    /**
     * Tests whether the given location 'collides' with the current component,
     * meaning whether the given location intersects either the component's registered region or on of the blocks associated with the current component
     * @param loc The location to be tested
     * @return Collision test result (true := collision, false := no collision)
     */
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

    /**
     * Returns whether the current component is an emulated processor
     * @return Indicates whether the current component is an emulated processor
     */
    public boolean isEmulatedProcessor()
    {
        return this instanceof EmulatedProcessor;
    }
    
    /**
     * Serialises the current component into the given YAML configuration
     * @param conf YAML configuration
     */
    public final void serialize(final YamlConfiguration conf)
    {
        serializeComponentSpecific(conf.getOrCreateSection("inner"));
        
        conf.set("orient", (int)orientation.getValue());
        conf.set("world", world == null ? "" : world.getUID());
        conf.set("x", x);
        conf.set("y", y);
        conf.set("z", z);
        conf.set("xs", xsize);
        conf.set("ys", ysize);
        conf.set("zs", zsize);
        conf.set("creator", creator == null ? "" : creator.getUniqueId());
        
        YamlConfiguration confIO = conf.getOrCreateSection("io");
        YamlConfiguration confBlocks = conf.getOrCreateSection("asscoblocks");
        
        confIO.set("count", getIOCount());
        
        for (int i = 0; i < getIOCount(); ++i)
        {
            confIO.set("port_" + i + ".value", io[i].getValue());
            confIO.set("port_" + i + ".direction", io[i].getDirection());
        }
        
        int num = 0;
        
        if (assocblocks != null)
            for (Triplet<Integer, Integer, Integer> block : assocblocks)
            {
                YamlConfiguration confBlock = confBlocks.getOrCreateSection("block_" + num);

                confBlock.put("x", block.x);
                confBlock.put("y", block.y);
                confBlock.put("z", block.z);
                
                ++num;
            }
        
        confBlocks.set("count", num);
    }
    
    /**
     * Deserialises the given YAML configuration into the current component
     * @param conf YAML configuration
     */
    public final void deserialize(final YamlConfiguration conf)
    {
        deserializeComponentSpecific(conf.getOrCreateSection("inner"));
        
        orientation = ComponentOrientation.fromValue((byte)conf.getInt("orient", 0));
        
        UUID uuid = conf.getUUID("creator", null);
        
        if (uuid != null)
            creator = MCPUCore.srv.getPlayer(uuid);
        
        world = MCPUCore.srv.getWorld(conf.getUUID("world", null));
        x = conf.getInt("x", 0);
        y = conf.getInt("y", 0);
        z = conf.getInt("z", 0);
        xsize = conf.getInt("xs", 0);
        ysize = conf.getInt("ys", 0);
        zsize = conf.getInt("zs", 0);

        YamlConfiguration confIO = conf.getOrCreateSection("io");
        YamlConfiguration confBlocks = conf.getOrCreateSection("asscoblocks");

        io = new IOPort[confIO.getInt("count", 0)];
        
        for (int i = 0; i < io.length; ++i)
            if (confIO.containsKey("port_" + i))
                io[i] = new IOPort(confIO.getInt("port_" + i + ".value", 0), confIO.getBoolean("port_" + i + ".direction", false));
        
        assocblocks = new ArrayList<>();
        
        for (int i = 0, cnt = confBlocks.getInt("count", 0); i < cnt; ++i)
            if (confBlocks.containsKey("block_" + i))
            {
                YamlConfiguration confBlock = confBlocks.getOrCreateSection("block_" + i);

                assocblocks.add(new Triplet<>(confBlock.getInt("x", 0), confBlock.getInt("y", 0), confBlock.getInt("z", 0)));
            }
        
        if (assocblocks.isEmpty())
            assocblocks = null;
    }
}
