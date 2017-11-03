package epsilonpotato.mcpu.core.components;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.IntegratedCircuit;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.util.Triplet;
import epsilonpotato.mcpu.util.YamlConfiguration;


public final class Multiplexer1to8 extends IntegratedCircuit
{
    private static final long serialVersionUID = 6750744897247302453L;

    
    public Multiplexer1to8(Player creator, Location loc, ComponentOrientation orient) throws InvalidOrientationException
    {
        super(creator, loc, new Triplet<>(7, 1, 16), 12, orient);
        
        for (int i = 0; i < 11; ++i)
            setIODirection(i, false);

        setIODirection(11, true);
    }

    @Override
    protected ComponentOrientation[] getValidOrientations()
    {
        return new ComponentOrientation[] { ComponentOrientation.NORTH };
    }

    @Override
    protected final void onTick()
    {
        int sel = 0;
        
        for (int i = 0; i < 3; i++)
            sel |= io[10 - i].isLow() ? 0 : 1 << i;
        
        io[11].setValue(io[sel].getValue());
    }

    @Override
    public final Location getIOLocation(int port)
    {
        if (port < 8)
            return new Location(world, x, y, z + 2 * port);
        else if (port < 11)
        {
            port -= 8;

            return new Location(world, x + 1 + 2 * port, y, z + 15);
        }
        else
            return new Location(world, x + 6, y, z + 7);
    }

    @Override
    public final String getState()
    {
        int sel = 0;
        
        for (int i = 0; i < 3; i++)
            sel |= io[10 - i].isLow() ? 0 : 1 << i;
        
        return String.format("%d (%s) --> out", sel, io[sel].isLow() ? "low" : "high");
    }


    @Override
    protected final void serializeComponentSpecific(YamlConfiguration conf)
    {
    }
    

    @Override
    protected final void deserializeComponentSpecific(YamlConfiguration conf)
    {   
    }
}
