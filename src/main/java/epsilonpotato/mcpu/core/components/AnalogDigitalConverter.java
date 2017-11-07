package epsilonpotato.mcpu.core.components;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.IntegratedCircuit;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.util.Triplet;
import epsilonpotato.mcpu.util.YamlConfiguration;

/**
 * Represents an A/D-converter (analog to digital converter)
 * @author Unknown6656
 */
public final class AnalogDigitalConverter extends IntegratedCircuit
{    private static final long serialVersionUID = 1L;


    // private static final long serialVersionUID = 5380148894828040315L;

    /**
     * Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     * @deprecated Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     */
    @Deprecated
    public AnalogDigitalConverter()
    {
        super();   
    }
    
    /**
     * Creates a new instance
     * @param creator The creator
     * @param loc The compontent's location
     * @throws InvalidOrientationException Thrown if the component was placed along an invalid orientation
     */
    public AnalogDigitalConverter(Player creator, Location loc) throws InvalidOrientationException
    {
        super(creator, loc, new Triplet<>(5, 1, 7), 5, ComponentOrientation.NORTH);
        
        for (int i = 0; i < 5; ++i)
            io[i].setDirection(i != 0);
    }
    
    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#serializeComponentSpecific(epsilonpotato.mcpu.util.YamlConfiguration)
     */
    @Override
    protected void serializeComponentSpecific(YamlConfiguration conf)
    {   
    }
    
    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#deserializeComponentSpecific(epsilonpotato.mcpu.util.YamlConfiguration)
     */
    @Override
    protected void deserializeComponentSpecific(YamlConfiguration conf)
    {
    }
    
    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#getValidOrientations()
     */
    @Override
    protected ComponentOrientation[] getValidOrientations()
    {
        return new ComponentOrientation[] { ComponentOrientation.NORTH };
    }
    
    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#onTick()
     */
    @Override
    protected void onTick()
    {
        byte val = io[0].getValue();
        
        for (int i = 0; i < 4; ++i)
            io[i + 1].setValue(((val >> i) & 1) != 0 ? 15 : 0);
    }
    
    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#getIOLocation(int)
     */
    @Override
    public Location getIOLocation(int port)
    {
        if (port == 0)
            return new Location(world, x, y, z + 3);
        else
            return new Location(world, x + 4, y, z + 2 * (port - 1));
    }
    
    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#getState()
     */
    @Override
    public String getState()
    {
        return "" + io[0].getValue();
    }
}
