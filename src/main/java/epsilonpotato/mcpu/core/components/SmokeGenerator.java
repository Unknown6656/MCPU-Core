package epsilonpotato.mcpu.core.components;

import java.io.IOException;  
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.IntegratedCircuit;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.util.BinaryReader;
import epsilonpotato.mcpu.util.BinaryWriter;
import epsilonpotato.mcpu.util.Triplet;


public class SmokeGenerator extends IntegratedCircuit
{
    public static final HashMap<ComponentOrientation, int[]> ports;


    static
    {
        ports = new HashMap<>();
        ports.put(ComponentOrientation.NORTH, new int[] { 0, 1 });
        ports.put(ComponentOrientation.WEST, new int[] { 1, 0 });
        ports.put(ComponentOrientation.SOUTH, new int[] { 2, 1 });
        ports.put(ComponentOrientation.EAST, new int[] { 1, 2 });
    }
    
    public SmokeGenerator(Player creator, Location loc, ComponentOrientation orient) throws InvalidOrientationException
    {
        super(creator, loc, new Triplet<>(3, 1, 3), 1, orient);
        
        setIODirection(0, false);
    }

    @Override
    protected ComponentOrientation[] getValidOrientations()
    {
        return new ComponentOrientation[] { ComponentOrientation.NORTH, ComponentOrientation.EAST, ComponentOrientation.WEST, ComponentOrientation.SOUTH };
    }

    @Override
    protected final void onTick()
    {
        if (!io[0].isLow())
            world.spawnParticle(Particle.SMOKE_LARGE, x + 1.5, y + 2, z + 1.5, 50, .5, 4, .5, 0);
    }

    @Override
    public final Location getIOLocation(int port)
    {
        return new Location(world, ports.get(orientation)[port * 2] + x, y, ports.get(orientation)[port * 2 + 1] + z); 
    }

    @Override
    public final String getState()
    {
        return io[0].isLow() ? "off" : "on";
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
