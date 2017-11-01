package epsilonpotato.mcpu.core.components;

import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.*;
import epsilonpotato.mcpu.util.*;


public class BinaryLogicGate extends IntegratedCircuit
{
    private static final long serialVersionUID = 5380136094828040844L;
    public static final HashMap<ComponentOrientation, int[]> ports;
    private BiFunction<Integer, Integer, Integer> func;
    private String name;


    static
    {
        ports = new HashMap<>();
        ports.put(ComponentOrientation.NORTH, new int[] { 0, 0, 0, 2, 2, 1 });
        ports.put(ComponentOrientation.WEST, new int[] { 0, 0, 2, 0, 1, 2 });
        ports.put(ComponentOrientation.SOUTH, new int[] { 2, 0, 2, 2, 0, 1 });
        ports.put(ComponentOrientation.EAST, new int[] { 0, 2, 2, 2, 1, 0 });
    }
    
    public BinaryLogicGate(Player creator, Location loc, BiFunction<Integer, Integer, Integer> func, String name, ComponentOrientation orient) throws InvalidOrientationException
    {
        super(creator, loc, new Triplet<>(3, 1, 3), 3, orient);
        
        this.func = func;
        this.name = name;

        setIODirection(0, false);
        setIODirection(1, false);
        setIODirection(2, true);
    }

    @Override
    protected ComponentOrientation[] getValidOrientations()
    {
        return new ComponentOrientation[] { ComponentOrientation.NORTH, ComponentOrientation.EAST, ComponentOrientation.WEST, ComponentOrientation.SOUTH };
    }

    @Override
    protected final void onTick()
    {
        int x = io[0].getValue();
        int y = io[1].getValue();
        int res = func.eval(x, y);
        
        io[2].setValue((res & 0xff) != 0 ? 15 : 0);
    }

    @Override
    public final Location getIOLocation(int port)
    {
        return new Location(world, ports.get(orientation)[port * 2] + x, y, ports.get(orientation)[port * 2 + 1] + z); 
    }

    @Override
    public final String getState()
    {
        return String.format("%s : %d, %d --> %d", name, io[0].getValue(), io[1].getValue(), io[2].getValue());
    }

    @Override
    protected void serializeComponentSpecific(BinaryWriter wr) throws IOException
    {
        byte[] delegate = Serializer.serialize(func);
        
        wr.write(name);
        wr.write(delegate.length);
        wr.write(delegate);
    }

    @Override
    protected void deserializeComponentSpecific(BinaryReader rd) throws IOException, ClassNotFoundException
    {
        name = rd.readString();
        
        int length = rd.readInt();
        byte[] delegate = rd.readBytes(length);

        func = Serializer.deserialize(delegate);
    }
}
