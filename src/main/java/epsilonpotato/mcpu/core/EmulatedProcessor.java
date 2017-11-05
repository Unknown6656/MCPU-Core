package epsilonpotato.mcpu.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.util.*;


public abstract class EmulatedProcessor extends IntegratedCircuit
{
    private static final long serialVersionUID = -3878716463587377556L;
    protected boolean canrun;
    protected long ticks;
    
    public EmulatedProcessorEvent<String> onError;


    protected abstract void innerStop();
    protected abstract void innerStart();
    protected abstract void innerReset();
    protected abstract void executeNextInstruction();
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
    
    public EmulatedProcessor(Player p, Location l, Triplet<Integer, Integer, Integer> size, int iocount, ComponentOrientation orient)
            throws InvalidOrientationException
    {
        super(p, l, size, iocount, orient);
        
        reset();
    }
    
    public final void reset()
    {
        stop();
    
        ticks = 0;
    
        innerReset();
    }

    public final void start()
    {
        innerStart();
        
        canrun = true;   
    }

    public final void stop()
    {
        canrun = false;
        
        innerStop();
    }

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
    
    @Override
    public final boolean isEmulatedProcessor()
    {
        return true;
    }
    
    public final long getTicksElapsed()
    {
        return ticks;
    }

    @Override
    public void onTick()
    {
        if (canrun)
        {
            executeNextInstruction();

            ++ticks;
        }
    }

    public final Location getNorthWestGoldBlock()
    {
        return getLocation().add(1, 0, 1);
    }

    protected final void getSign(Consumer<Sign> f)
    {
        Block b = getSignLocation().getBlock();
        
        if (b != null)
        {
            Sign s = (Sign)b.getState();
            
            f.accept(s);
            s.update();
        }
    }

    public final Location getSignLocation()
    {
        return getLocation().add(1, 1, 1);
    }

    @Override
    protected void deserializeComponentSpecific(final YamlConfiguration conf)
    {
        ticks = conf.getLong("ticks", 0);
        canrun = conf.getBoolean("canrun", false);
    }
    
    @Override
    protected void serializeComponentSpecific(final YamlConfiguration conf)
    {
        conf.set("ticks", ticks);
        conf.set("canrun", canrun);
    }
}
