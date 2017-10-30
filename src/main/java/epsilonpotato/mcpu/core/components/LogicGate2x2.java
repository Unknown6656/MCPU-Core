package epsilonpotato.mcpu.core.components;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.IntegratedCircuit;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.util.BinaryReader;
import epsilonpotato.mcpu.util.BinaryWriter;
import epsilonpotato.mcpu.util.Serializer;
import epsilonpotato.mcpu.util.Triplet;
import epsilonpotato.mcpu.util.Tuple;

public class LogicGate2x2 extends IntegratedCircuit
{
    public static final HashMap<ComponentOrientation, int[]> ports;
    private Consumer<Tuple<int[], byte[]>> func;
    private byte[] state = new byte[16];
    private String name;
    

    static
    {
        ports = new HashMap<>();
        ports.put(ComponentOrientation.NORTH, new int[] { 0, 0, 0, 2, 3, 0, 3, 2 });
        ports.put(ComponentOrientation.WEST, new int[] { 2, 0, 0, 0, 2, 3, 0, 3 });
        ports.put(ComponentOrientation.SOUTH, new int[] { 3, 2, 3, 0, 0, 2, 0, 0 });
        ports.put(ComponentOrientation.EAST, new int[] { 0, 3, 2, 3, 0, 0, 2, 0 });
    }

    public LogicGate2x2(Player creator, Location loc, Consumer<Tuple<int[], byte[]>> func, String name, ComponentOrientation orient) throws InvalidOrientationException
    {
        super(creator, loc, orient.isNorthSouth() ? new Triplet<>(4, 1, 3) : new Triplet<>(3, 1, 4), 4, orient);
        
        this.func = func;
        this.name = name;

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
        
        func.accept(new Tuple<int[], byte[]>(data, state));

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
        return String.format("%s : %d, %d --> %d, %d", name, io[0].getValue(), io[1].getValue(), io[2].getValue(), io[3].getValue());
    }

    @Override
    protected void serializeComponentSpecific(BinaryWriter wr) throws IOException
    {
        byte[] delegate = Serializer.serialize((Consumer<Tuple<int[], byte[]>> & Serializable)func);
        
        wr.write(name);
        wr.write(delegate.length);
        wr.write(delegate);
        wr.write(state);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void deserializeComponentSpecific(BinaryReader rd) throws IOException, ClassNotFoundException
    {
        name = rd.readString();
        
        int length = rd.readInt();
        byte[] delegate = rd.readBytes(length);

        func = (Consumer<Tuple<int[], byte[]>>)Serializer.deserialize(delegate);
        state = rd.readBytes(16);
    }
}
