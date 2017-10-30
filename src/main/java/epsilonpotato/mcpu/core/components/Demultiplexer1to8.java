package epsilonpotato.mcpu.core.components;

import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.IntegratedCircuit;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.util.BinaryReader;
import epsilonpotato.mcpu.util.BinaryWriter;
import epsilonpotato.mcpu.util.Triplet;


public final class Demultiplexer1to8 extends IntegratedCircuit
{
    public Demultiplexer1to8(Player creator, Location loc, ComponentOrientation orient) throws InvalidOrientationException
    {
        super(creator, loc, new Triplet<>(7, 1, 16), 12, orient);
        
        for (int i = 0; i < 12; ++i)
            setIODirection(i, i > 3);
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
            sel |= io[3 - i].isLow() ? 0 : 1 << i;

        io[11 - sel].setValue(io[0].getValue());
    }

    @Override
    public final Location getIOLocation(int port)
    {
        if (port == 0)
            return new Location(world, x, y, z + 7);
        else if (port < 4)
        {
            port = 3 - port;
            
            return new Location(world, x + 1 + 2 * port, y, z + 15);
        }
        else
            return new Location(world, x + 6, y, z + 2 * (11 - port));
    }

    @Override
    public final String getState()
    {
        int sel = 0;
        
        for (int i = 0; i < 3; i++)
            sel |= io[3 - i].isLow() ? 0 : 1 << i;
        
        return String.format("in (%s) --> %d", io[0].isLow() ? "low" : "high", sel);
    }

    @Override
    protected void serializeComponentSpecific(BinaryWriter wr) throws IOException
    {
    }

    @Override
    protected void deserializeComponentSpecific(BinaryReader rd) throws IOException, ClassNotFoundException
    {
    }
}
