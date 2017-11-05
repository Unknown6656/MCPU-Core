package epsilonpotato.mcpu.core;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.util.*;


/**
 * Represents an abstract square lever-aware emulated processor
 * @author Unknown6656
 */
public abstract class SquareEmulatedProcessor extends LeverAwareEmulatedProcessor
{
    private static final long serialVersionUID = -78130561939840819L;
    /**
     * The number of I/O ports on each of the 4 sides
     */
    protected int sidecount; 


    /**
     * Serialises processor-specific data into the given YAML configuration
     * @param conf YAML configuration
     */
    protected abstract void deserializeProcessorState(final YamlConfiguration conf);
    /**
     * Deserialises processor-specific data from the given YAML configuration
     * @param conf YAML configuration
     */
    protected abstract void serializeProcessorState(final YamlConfiguration conf);
    

    /**
     * Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     * @deprecated Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     */
    @Deprecated
    public SquareEmulatedProcessor()
    {
         super();
    }
    
    /**
     * Creates a new instance
     * @param p The creator
     * @param l The processor's location
     * @param iosidecount The number of I/O ports on each of the 4 processor's sides
     * @param orient The component's desired orientation
     * @throws InvalidOrientationException Thrown, if the component was placed along an invalid orientation [never thrown, only inherited form parent class]
     */
    public SquareEmulatedProcessor(Player p, Location l, int iosidecount)
            throws InvalidOrientationException
    {
        super(p, l, new Triplet<>(iosidecount * 2 + 1, 2, iosidecount * 2 + 1), iosidecount * 4, ComponentOrientation.NORTH);
        
        sidecount = iosidecount;
    }
    
    /**
     * @see epsilonpotato.mcpu.core.LeverAwareEmulatedProcessor#getLeverLocation()
     */
    @Override
    public final Location getLeverLocation()
    {
        return new Location(world, x + 2, y, z + 1);
    }

    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#getValidOrientations()
     */
    @Override
    protected final ComponentOrientation[] getValidOrientations()
    {
        return new ComponentOrientation[] { ComponentOrientation.NORTH };
    }

    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#getIOLocation(int)
     */
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
    
    /**
     * @see epsilonpotato.mcpu.core.EmulatedProcessor#serializeComponentSpecific(epsilonpotato.mcpu.util.YamlConfiguration)
     */
    @Override
    protected final void serializeComponentSpecific(final YamlConfiguration conf)
    {
        conf.set("ticks", ticks);
        conf.set("canrun", canrun);
        conf.set("sidecount", sidecount);

        serializeProcessorState(conf.getOrCreateSection("specific"));
    }
    
    /**
     * @see epsilonpotato.mcpu.core.EmulatedProcessor#deserializeComponentSpecific(epsilonpotato.mcpu.util.YamlConfiguration)
     */
    @Override
    protected final void deserializeComponentSpecific(final YamlConfiguration conf)
    {
        ticks = conf.getLong("ticks", 0);
        canrun = conf.getBoolean("canrun", false);
        sidecount = conf.getInt("sidecount", 0);

        deserializeProcessorState(conf.getOrCreateSection("specific"));
    }
}
