package epsilonpotato.mcpu.core.components;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.BlockPlacingContext;
import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.IntegratedCircuit;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.util.Triplet;
import epsilonpotato.mcpu.util.YamlConfiguration;

/*
 * 4 pins x coord:  0..3
 * 4 pins z coord:  4..7
 * 4 pins color:    8..11
 * 1 pin color set: 12
 * 1 pin reset all: 13 
 * */
public final class WoolDisplay16x16 extends IntegratedCircuit
{
    private static final long serialVersionUID = 7014314217678821492L;
    protected static final Material base_material = Material.CONCRETE;
    private byte[][] matrix = new byte[16][16];
            
    
    public WoolDisplay16x16(Player p, World w, int x, int y, int z) throws InvalidOrientationException
    {
        super(p, new Location(w, x, y, z), new Triplet<>(19, 1, 19), 14, ComponentOrientation.NORTH);

        for (int i = 0; i < 16; ++i)
            for (int j = 0; j < 16; ++j)
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
        if (port < 4)
            return new Location(world, x, y, z + 1 + 2 * port);
        else if (port < 8)
            return new Location(world, x, y, z + 10 + 2 * (port - 4));
        else if (port < 12)
            return new Location(world, x + 2 * (port - 7), y, z + 18);
        else
            return new Location(world, x + 11 + 2 * (port - 12), y, z + 18);
    }

    @Override
    protected void onTick()
    {
        if (io[13].isLow())
        {
            if (!io[12].isLow())
            {
                int x = 0, y = 0, c = 0;
                
                for (int i = 0; i < 4; ++i)
                {
                    x |= (io[i].isLow() ? 0 : 1) << (3 - i);
                    y |= (io[i + 4].isLow() ? 0 : 1) << (3 - i);
                    c |= (io[i + 8].isLow() ? 0 : 1) << (3 - i);
                }

                matrix[y][x] = (byte)Math.min(c, 15);

                updateDisplay(y, x);
            }
        }
        else
            for (int i = 0; i < 16; ++i)
                for (int j = 0; j < 16; ++j)
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
    protected final void serializeComponentSpecific(final YamlConfiguration conf)
    {
        for (int y = 0; y < 16; ++y)
            for (int x = 0; x < 16; ++x)
                conf.set("x" + x + "_y" + y, (int)matrix[y][x]);
    }
    
    @Override
    protected final void deserializeComponentSpecific(final YamlConfiguration conf)
    {
        for (int y = 0; y < 16; ++y)
            for (int x = 0; x < 16; ++x)
                matrix[y][x] = (byte)conf.getInt("x" + x + "_y" + y, 0);
    }

    @Override
    public String getState()
    {
        return "//TODO//";
    }
}
