package epsilonpotato.mcpu.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;


public abstract class EmulatedProcessor extends IntegratedCircuit
{
    protected boolean canrun;
    protected long ticks;
    
    public EmulatedProcessorEvent<String> onError;


    protected abstract void innerStop();
    protected abstract void innerStart();
    protected abstract void innerReset();
    protected abstract void executeNextInstruction();
    public abstract boolean load(String code);


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
        StringBuilder code = new StringBuilder();

        try
        {
            String s = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(source.toString()).openStream()));
            
            while ((s = reader.readLine()) != null)
                code.append('\n').append(s);
        }
        catch (Exception e1)
        {
            if (source.getScheme().toLowerCase().equals("raw"))
                code.append(source.getPath());
            else
                // TODO : dunno ?
                return false;
        }
        
        return load(code.toString());
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
}
