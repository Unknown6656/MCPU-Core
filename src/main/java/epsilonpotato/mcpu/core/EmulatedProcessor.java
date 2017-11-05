package epsilonpotato.mcpu.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Base64;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.util.*;


/**
 * Represents an abstract emulated processor
 * @author Unknown6656
 */
public abstract class EmulatedProcessor extends IntegratedCircuit
{
    private static final long serialVersionUID = -3878716463587377556L;
    /**
     * Indicates whether the emulated processor can currently run
     */
    protected boolean canrun;
    /**
     * The number of elapsed ticks since the last reset
     */
    protected long ticks;
    
    /**
     * The processor's error event delegate/handler
     */
    public EmulatedProcessorEvent<String> onError;


    /**
     * The processor's inner stopping method
     */
    protected abstract void innerStop();
    /**
     * The processor's inner starting method
     */
    protected abstract void innerStart();
    /**
     * The processor's inner reset method
     */
    protected abstract void innerReset();
    /**
     * The processor's inner handler which will be called when the next instruction is ready to be executed
     */
    protected abstract void executeNextInstruction();
    /**
     * Loads the given source code into the current processor and returns whether the operation was successful
     * @param code Source code
     * @return Operation success result
     */
    public abstract boolean load(String code);


    /**
     * Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     * @deprecated Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     */
    @Deprecated
    public EmulatedProcessor()
    {
         super();
    }
    
    /**
     * Creates a new instance
     * @param p The creator
     * @param l The compontent's location
     * @param size The component's size
     * @param iocount The component's I/O port count
     * @param orient The component's desired orientation
     * @throws InvalidOrientationException Thrown, if the component was placed along an invalid orientation
     */
    public EmulatedProcessor(Player p, Location l, Triplet<Integer, Integer, Integer> size, int iocount, ComponentOrientation orient)
            throws InvalidOrientationException
    {
        super(p, l, size, iocount, orient);
        
        reset();
    }
    
    /**
     * Resets the processor to its original state
     */
    public final void reset()
    {
        stop();
    
        ticks = 0;
    
        innerReset();
    }

    /**
     * Starts the processor
     */
    public final void start()
    {
        innerStart();
        
        canrun = true;   
    }

    /**
     * Stops the processor
     */
    public final void stop()
    {
        canrun = false;
        
        innerStop();
    }

    /**
     * Compiles the code given by the URI into the processor
     * @param source Code to be compiled/loaded
     * @return 'true', if the operation was successful - otherwise false
     */
    public boolean load(URI source)
    {
        String code;

        try
        {
            String s = null;
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(source.toString()).openStream()));
            
            while ((s = reader.readLine()) != null)
                sb.append('\n').append(s);
            
            code = sb.toString();
        }
        catch (Exception e1)
        {
            if (source.getScheme().toLowerCase().equals("data"))
            {
                String b64 = source.toString();
                int commandx = b64.indexOf(',');
                
                b64 = b64.substring(commandx + 1);

                Base64.Decoder dec = Base64.getDecoder();

                code = new String(dec.decode(b64));
            }
            else
                // TODO : dunno ?
                return false;
        }
        
        return load(code);
    }
    
    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#isEmulatedProcessor()
     */
    @Override
    public final boolean isEmulatedProcessor()
    {
        return true;
    }
    
    /**
     * Returns the number of elapsed processor ticks since the last reset
     * @return Elapsed ticks
     */
    public final long getTicksElapsed()
    {
        return ticks;
    }

    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#onTick()
     */
    @Override
    public void onTick()
    {
        if (canrun)
        {
            executeNextInstruction();

            ++ticks;
        }
    }

    /**
     * Returns the north-west corner of the processor (usually a gold block)
     * @return
     */
    public final Location getNorthWestGoldBlock()
    {
        return getLocation().add(1, 0, 1);
    }

    /**
     * Fetches the data associated with the sign placed on the processor and invokes the given callback method with the fetched data
     * @param f Callback method
     */
    protected final void getSign(Action<Sign> f)
    {
        Block b = getSignLocation().getBlock();
        
        if (b != null)
        {
            Sign s = (Sign)b.getState();
            
            f.eval(s);
            s.update();
        }
    }

    /**
     * Returns the sign's location (usually placed over the gold block in the north-west corner of the processor)
     * @return The sign's location
     */
    public final Location getSignLocation()
    {
        return getLocation().add(1, 1, 1);
    }

    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#deserializeComponentSpecific(epsilonpotato.mcpu.util.YamlConfiguration)
     */
    @Override
    protected void deserializeComponentSpecific(final YamlConfiguration conf)
    {
        ticks = conf.getLong("ticks", 0);
        canrun = conf.getBoolean("canrun", false);
    }
    
    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#serializeComponentSpecific(epsilonpotato.mcpu.util.YamlConfiguration)
     */
    @Override
    protected void serializeComponentSpecific(final YamlConfiguration conf)
    {
        conf.set("ticks", ticks);
        conf.set("canrun", canrun);
    }
}
