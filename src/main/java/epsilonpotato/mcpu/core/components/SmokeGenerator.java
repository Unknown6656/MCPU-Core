package epsilonpotato.mcpu.core.components;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.IntegratedCircuit;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.util.Triplet;
import epsilonpotato.mcpu.util.YamlConfiguration;


public class SmokeGenerator extends IntegratedCircuit
{
    private static final long serialVersionUID = -3302634901119237082L;
    public static final HashMap<ComponentOrientation, int[]> ports;


    static
    {
        ports = new HashMap<>();
        ports.put(ComponentOrientation.NORTH, new int[] { 0, 1 });
        ports.put(ComponentOrientation.WEST, new int[] { 1, 0 });
        ports.put(ComponentOrientation.SOUTH, new int[] { 2, 1 });
        ports.put(ComponentOrientation.EAST, new int[] { 1, 2 });
    }

    /**
     * Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     * @deprecated Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     */
    @Deprecated
    public SmokeGenerator()
    {
         super();
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
            world.spawnParticle(Particle.SMOKE_LARGE, x + 1.5, y + 3.5, z + 1.5, 50, .5, 4, .5, 0);
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
    protected final void serializeComponentSpecific(YamlConfiguration conf)
    {
    }

    
    @Override
    protected final void deserializeComponentSpecific(YamlConfiguration conf)
    {
    }
}
