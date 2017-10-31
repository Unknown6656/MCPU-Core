package epsilonpotato.mcpu.core.components;

import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.BlockPlacingContext;
import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.IntegratedCircuit;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.util.BinaryReader;
import epsilonpotato.mcpu.util.BinaryWriter;
import epsilonpotato.mcpu.util.Triplet;

/*
 * 5 pins x coord:  0..4
 * 5 pins z coord:  5..9
 * 4 pins color:    10..13
 * 1 pin color set: 14
 * 1 pin reset all: 15 
 * */
public final class WoolDisplay32x32 extends IntegratedCircuit
{
    private static final long serialVersionUID = 8742455457965007648L;
    protected static final Material base_material = Material.CONCRETE;
    private byte[][] matrix = new byte[32][32];
            
    
    public WoolDisplay32x32(Player p, World w, int x, int y, int z) throws InvalidOrientationException
    {
        super(p, new Location(w, x, y, z), new Triplet<>(35, 1, 34), 16, ComponentOrientation.NORTH);

        for (int i = 0; i < 32; ++i)
            for (int j = 0; j < 32; ++j)
                updateDisplay(i, j);
    }

    @SuppressWarnings("deprecation")
    private void updateDisplay(int yoffs, int xoffs)
    {
        new BlockPlacingContext(world).addBlock(x + xoffs + 2, y, z + yoffs + 1, base_material, b -> b.setData(matrix[yoffs][xoffs]));
    }
    
    @Override
    public Location getIOLocation(int port)
    {
        if (port < 10)
            return new Location(world, x, y, z + 1 + 2 * port);
        else
            return new Location(world, x, y, z + 22 + 2 * (port - 10));
    }

    @Override
    protected void onTick()
    {
        if (io[15].isLow())
        {
            if (!io[14].isLow())
            {
                int x = 0, y = 0, c = 0;
                
                for (int i = 0; i < 5; ++i)
                {
                    x |= (io[i].isLow() ? 0 : 1) << (3 - i);
                    y |= (io[i + 5].isLow() ? 0 : 1) << (3 - i);
                    c |= (io[i + 10].isLow() ? 0 : 1) << (3 - i);
                }
                
                matrix[y][x] = (byte)Math.min(c, 15);

                updateDisplay(y, x);
            }
        }
        else
            for (int i = 0; i < 32; ++i)
                for (int j = 0; j < 32; ++j)
                {
                    matrix[i][j] = 0;

                    updateDisplay(i, j);
                }
    }

    @Override
    protected ComponentOrientation[] getValidOrientations()
    {
        return new ComponentOrientation[] { ComponentOrientation.NORTH };
    }

    @Override
    protected void serializeComponentSpecific(BinaryWriter wr) throws IOException
    {
        for (int i = 0; i < 32; ++i)
            for (int j = 0; j < 32; ++j)
                wr.write(matrix[i][j]);
    }
    
    @Override
    protected void deserializeComponentSpecific(BinaryReader rd) throws IOException
    {
        for (int i = 0; i < 32; ++i)
            for (int j = 0; j < 32; ++j)
                matrix[i][j] = rd.readByte();
    }

    
    @Override
    public String getState()
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 32; ++i)
        {
            for (int j = 0; j < 32; ++j)
                sb.append(String.format("%x", matrix[i][j]));
            
            if (i < 31)
                sb.append('\n');
        }
        
        return sb.toString();
    }
}
