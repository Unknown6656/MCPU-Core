
package epsilonpotato.mcpu.core.components.factories;


import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.*;
import epsilonpotato.mcpu.util.*;
import epsilonpotato.mcpu.core.components.BinaryLogicGate;
import epsilonpotato.mcpu.core.components.BinaryLogicGateType;

import java.util.HashMap;


/**
 * An factory to create <pre>[0..15] x [0..15] -> [0..15]</pre> logic gates
 * @author Unknown6656
 */
@SuppressWarnings("unused")
public final class BinaryLogicGateFactory extends ComponentFactory<BinaryLogicGate>
{
    private static final HashMap<ComponentOrientation, Tuple<Integer, Integer>> signloc;
    private final BinaryLogicGateType type;
    
    static
    {
        signloc = new HashMap<>();
        signloc.put(ComponentOrientation.NORTH, new Tuple<>(0, 1));
        signloc.put(ComponentOrientation.WEST, new Tuple<>(1, 0));
        signloc.put(ComponentOrientation.SOUTH, new Tuple<>(2, 1));
        signloc.put(ComponentOrientation.EAST, new Tuple<>(1, 2));
    }
    
    
    /**
     * Creates a new instance
     * @param type Logic gate type
     */
    public BinaryLogicGateFactory(BinaryLogicGateType type)
    {
        this.type = type;
    }
    
    /**
     * @see epsilonpotato.mcpu.core.ComponentFactory#getEstimatedSize(epsilonpotato.mcpu.core.ComponentOrientation)
     */
    @Override
    public Triplet<Integer, Integer, Integer> getEstimatedSize(ComponentOrientation or)
    {
        return or.isUpright() ? or.isNorthSouth() ? new Triplet<>(1, 3, 5) : new Triplet<>(5, 3, 1) : new Triplet<>(3, 1, 3);
    }
    
    /**
     * @see epsilonpotato.mcpu.core.ComponentFactory#spawnComponent(epsilonpotato.mcpu.core.BlockPlacingContext, epsilonpotato.mcpu.core.MCPUCore, org.bukkit.entity.Player, int, int, int, epsilonpotato.mcpu.core.ComponentOrientation, int)
     */
    @Override
    @SuppressWarnings("deprecation")
    public BinaryLogicGate spawnComponent(BlockPlacingContext context, MCPUCore caller, Player p, int x, int y, int z, ComponentOrientation or, int iocount)
            throws InvalidOrientationException
    {
        int[] pinloc = BinaryLogicGate.ports.get(or);
        boolean ns = or.isNorthSouth();
       
        // CREATE WOOL FRAME
        if (or.isUpright())
            createWoolFrame(context, x, y + 1, z, or.isNorthSouth() ? 1 : 5, 2, or.isNorthSouth() ? 5 : 1);
        else
            for (int i = 0; i < 3; ++i)
                context.addBlock(x + (ns ? 1 : i), y, z + (ns ? i : 1), Material.WOOL, b -> b.setData(DyeColor.BLACK.getWoolData())); // TODO: fix deprecated calls
        
        // CREATE PINS
        for (int i = 0; i < 3; ++i)
            context.addBlock(x + pinloc[2 * i], y, z + pinloc[2 * i + 1], Material.IRON_BLOCK);
        
        // CREATE SIGN
        /*
        context.addBlock(y + signloc.get(or).x, y, z + signloc.get(or).y, Material.WALL_SIGN, b ->
        {
            Sign sign = (Sign)b.getState();
            
            sign.setLine(0, "BinaryGate");
            sign.setLine(1, type.toString());
            sign.setLine(2, p.getDisplayName());
            sign.update();
        });
        */
        
        return new BinaryLogicGate(p, new Location(context.getWorld(), x, y, z), type, or);
    }
}
