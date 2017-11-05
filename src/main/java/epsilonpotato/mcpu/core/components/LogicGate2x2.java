package epsilonpotato.mcpu.core.components;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.*;
import epsilonpotato.mcpu.util.*;

public class LogicGate2x2 extends IntegratedCircuit
{
    private static final long serialVersionUID = -8450797721098705457L;
    public static final HashMap<ComponentOrientation, int[]> ports;
    private byte[] state = new byte[16];
    private LogicGate2x2Type type;
    

    static
    {
        ports = new HashMap<>();
        ports.put(ComponentOrientation.NORTH, new int[] { 0, 0, 0, 2, 3, 0, 3, 2 });
        ports.put(ComponentOrientation.WEST, new int[] { 2, 0, 0, 0, 2, 3, 0, 3 });
        ports.put(ComponentOrientation.SOUTH, new int[] { 3, 2, 3, 0, 0, 2, 0, 0 });
        ports.put(ComponentOrientation.EAST, new int[] { 0, 3, 2, 3, 0, 0, 2, 0 });
    }

    /**
     * Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     * @deprecated Do NOT use the empty constructor!! It is only there for YAML serialisation/deserialisation
     */
    @Deprecated
    public LogicGate2x2()
    {
         super();
    }
    
    public LogicGate2x2(Player creator, Location loc, LogicGate2x2Type type, ComponentOrientation orient) throws InvalidOrientationException
    {
        super(creator, loc, orient.isNorthSouth() ? new Triplet<>(4, 1, 3) : new Triplet<>(3, 1, 4), 4, orient);
        
        this.type = type;

        setIODirection(0, false);
        setIODirection(1, false);
        setIODirection(2, true);
        setIODirection(3, true);
    }

    @Override
    protected ComponentOrientation[] getValidOrientations()
    {
        return new ComponentOrientation[] { ComponentOrientation.NORTH, ComponentOrientation.EAST, ComponentOrientation.WEST, ComponentOrientation.SOUTH };
    }

    @Override
    protected final void onTick()
    {
        int[] data = new int[] { io[0].getValue(), io[1].getValue(), 0, 0 };
        
        type.eval(data, state);

        io[2].setValue((data[2] & 0xff) != 0 ? 15 : 0);
        io[3].setValue((data[3] & 0xff) != 0 ? 15 : 0);
    }

    @Override
    public final Location getIOLocation(int port)
    {
        return new Location(world, ports.get(orientation)[port * 2] + x, y, ports.get(orientation)[port * 2 + 1] + z); 
    }

    @Override
    public final String getState()
    {
        return String.format("%s : %d, %d --> %d, %d", type.toString(), io[0].getValue(), io[1].getValue(), io[2].getValue(), io[3].getValue());
    }

    @Override
    protected void serializeComponentSpecific(final YamlConfiguration conf)
    {
        conf.set("type", type.toString());

        YamlConfiguration confState = conf.getOrCreateSection("state");
        
        for (int i = 0; i < state.length; ++i)
            confState.set("slot_" + i, state[i]);
    }

    @Override
    protected void deserializeComponentSpecific(final YamlConfiguration conf)
    {
        type = LogicGate2x2Type.valueOf(conf.getString("type", ""));
        
        YamlConfiguration confState = conf.getOrCreateSection("state");
        
        state = new byte[16];

        for (int i = 0; i < state.length; ++i)
            if (confState.containsKey("slot_" + i))
                state[i] = (byte)confState.getInt("slot_" + i, 0);
    }
}
