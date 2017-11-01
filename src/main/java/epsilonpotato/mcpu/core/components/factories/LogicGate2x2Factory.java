
package epsilonpotato.mcpu.core.components.factories;


import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.*;
import epsilonpotato.mcpu.util.*;
import epsilonpotato.mcpu.core.components.LogicGate2x2;

import java.util.HashMap;


public final class LogicGate2x2Factory extends ComponentFactory<LogicGate2x2>
{
    private static final HashMap<ComponentOrientation, Tuple<Integer, Integer>> signloc;
    private final BiAction<int[], byte[]> func;
    private final String name;
    
    static
    {
        signloc = new HashMap<>();
        signloc.put(ComponentOrientation.NORTH, new Tuple<>(0, 1));
        signloc.put(ComponentOrientation.WEST, new Tuple<>(1, 0));
        signloc.put(ComponentOrientation.SOUTH, new Tuple<>(2, 1));
        signloc.put(ComponentOrientation.EAST, new Tuple<>(1, 2));
    }
    
    
    public LogicGate2x2Factory(BiAction<int[], byte[]> func, String name)
    {
        this.func = func;
        this.name = name;
    }
    
    @Override
    public Triplet<Integer, Integer, Integer> getEstimatedSize(ComponentOrientation or)
    {
        return or.isNorthSouth() ? new Triplet<>(4, 1, 3) : new Triplet<>(3, 1, 4);
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public LogicGate2x2 spawnComponent(BlockPlacingContext context, MCPUCore caller, Player p, int x, int y, int z, ComponentOrientation or, int iocount)
            throws InvalidOrientationException
    {
        int[] pinloc = LogicGate2x2.ports.get(or);
        boolean ns = or.isNorthSouth();
        
        // CREATE WOOL FRAME
        for (int i = 0; i < 3; ++i)
        {
            context.addBlock(x + (ns ? 1 : i), y, z + (ns ? i : 1), Material.WOOL, b -> b.setData(DyeColor.BLACK.getWoolData())); // TODO: fix deprecated calls
            context.addBlock(x + (ns ? 2 : i), y, z + (ns ? i : 2), Material.WOOL, b -> b.setData(DyeColor.BLACK.getWoolData())); // TODO: fix deprecated calls
        }
            
        // CREATE PINS
        for (int i = 0; i < 4; ++i)
            context.addBlock(x + pinloc[2 * i], y, z + pinloc[2 * i + 1], Material.IRON_BLOCK);

        /*
        // CREATE SIGN
        context.addBlock(y + signloc.get(or).x, y, z + signloc.get(or).y, Material.WALL_SIGN, b ->
        {
            Sign sign = (Sign)b.getState();
            
            sign.setLine(0, "BinaryGate");
            sign.setLine(1, name);
            sign.setLine(2, p.getDisplayName());
            sign.update();
        });
        */
        
        return new LogicGate2x2(p, new Location(context.getWorld(), x, y, z), func, name, or);
    }
}
