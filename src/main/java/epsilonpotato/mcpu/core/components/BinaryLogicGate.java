package epsilonpotato.mcpu.core.components;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.*;
import epsilonpotato.mcpu.util.*;


/**
 * Represents a binary logic gate which processes inputs with the following signature:
 * <pre>[0..15] x [0..15] --> [0..15]</pre>
 * @author Unknown6656
 */
public final class BinaryLogicGate extends IntegratedCircuit
{
    private static final long serialVersionUID = 5380136094828040844L;
    /**
     * A list of port locations depending on the component's orientation
     */
    public static final HashMap<ComponentOrientation, int[]> ports;
    private BinaryLogicGateType type;


    static
    {
        ports = new HashMap<>();
        ports.put(ComponentOrientation.NORTH, new int[] { 0, 0, 0, 2, 2, 1 });
        ports.put(ComponentOrientation.WEST, new int[] { 0, 0, 2, 0, 1, 2 });
        ports.put(ComponentOrientation.SOUTH, new int[] { 2, 0, 2, 2, 0, 1 });
        ports.put(ComponentOrientation.EAST, new int[] { 0, 2, 2, 2, 1, 0 });
        ports.put(ComponentOrientation.UPRIGHT_EAST_WEST, new int[] { 0, 0, 2, 0, 4, 0 });
        ports.put(ComponentOrientation.UPRIGHT_NORTH_SOUTH, new int[] { 0, 0, 0, 2, 0, 4 });
    }

    /**
     * Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     * @deprecated Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     */
    @Deprecated
    public BinaryLogicGate()
    {
         super();
    }
    
    /**
     * Creates a new binary logic gate
     * @param creator The creator
     * @param type The binary logic gate type
     * @param loc The gate's location
     * @param orient The gate's desired orientation
     * @throws InvalidOrientationException Thrown, if the gate was placed along an invalid orientation
     */
    public BinaryLogicGate(Player creator, Location loc, BinaryLogicGateType type, ComponentOrientation orient) throws InvalidOrientationException
    {
        super(creator, loc, ComponentFactory.getFactory(BinaryLogicGate.class).getEstimatedSize(orient), 3, orient);
        
        this.type = type;

        setIODirection(0, false);
        setIODirection(1, false);
        setIODirection(2, true);
    }

    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#getValidOrientations()
     */
    @Override
    protected ComponentOrientation[] getValidOrientations()
    {
        return new ComponentOrientation[] { ComponentOrientation.NORTH, ComponentOrientation.EAST, ComponentOrientation.WEST, ComponentOrientation.SOUTH };
    }

    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#onTick()
     */
    @Override
    protected final void onTick()
    {
        int x = io[0].getValue();
        int y = io[1].getValue();
        int res = type.eval(x, y);
        
        io[2].setValue((res & 0xff) != 0 ? 15 : 0);
    }

    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#getIOLocation(int)
     */
    @Override
    public final Location getIOLocation(int port)
    {
        return new Location(world, ports.get(orientation)[port * 2] + x, y, ports.get(orientation)[port * 2 + 1] + z); 
    }

    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#getState()
     */
    @Override
    public final String getState()
    {
        return String.format("%s : %d, %d --> %d", type.toString(), io[0].getValue(), io[1].getValue(), io[2].getValue());
    }

    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#serializeComponentSpecific(epsilonpotato.mcpu.util.YamlConfiguration)
     */
    @Override
    protected final void serializeComponentSpecific(YamlConfiguration conf)
    {
        conf.put("type", type);
    }

    /**
     * @see epsilonpotato.mcpu.core.IntegratedCircuit#deserializeComponentSpecific(epsilonpotato.mcpu.util.YamlConfiguration)
     */
    @Override
    protected final void deserializeComponentSpecific(YamlConfiguration conf)
    {
        type = BinaryLogicGateType.valueOf(conf.getString("type", ""));
    }
}
