package epsilonpotato.mcpu.core;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import static epsilonpotato.mcpu.core.MCPUCore.*;


public abstract class SquareEmulatedProcessorFactory<T extends SquareEmulatedProcessor> extends ComponentFactory<SquareEmulatedProcessor>
{
    public abstract T createProcessor(BlockPlacingContext context, MCPUCore caller, Player p, int x, int y, int z, int iosidecount);
    
    
    @Override
    @SuppressWarnings("deprecation")
    public final SquareEmulatedProcessor spawnComponent(BlockPlacingContext context, MCPUCore caller, Player p, int x, int y, int z, ComponentOrientation or, int iosidecount)
            throws InvalidOrientationException
    {
        if (or != ComponentOrientation.NORTH)
            throw new InvalidOrientationException(or, ComponentOrientation.NORTH);
        
        int sidelength = iosidecount * 2 - 1;
        
        x += 1;
        z += 1;
        
        deleteRegion(context.getWorld(), x - 2, y - 1, z - 2, sidelength + 4, 3, sidelength + 4);
        createBase(context, x - 2, y - 1, z - 2, sidelength + 4, sidelength + 4);
        
        // CREATE WOOL BODY
        for (int i = 0; i < sidelength; ++i)
            for (int j = 0; j < sidelength; ++j)
                context.addBlock(x + i, y, z + j, Material.WOOL, b -> b.setData(DyeColor.BLACK.getWoolData())); // TODO: fix deprecated calls
        
        // CREATE GOLD CORNER + SIGN + LEVER
        int num = circuits.size();
        
        context.addBlock(x, y, z, Material.GOLD_BLOCK);
        context.addBlock(x + 1, y, z, Material.GOLD_BLOCK);
        context.addBlock(x + 1, y + 1, z, Material.LEVER, b ->
        {
            b.setData((byte)6);
            b.getState().update();
        });
        context.addBlock(x, y + 1, z, Material.SIGN_POST, b ->
        {
            Sign sign = (Sign)b.getState();
            
            sign.setLine(0, "CPU No. " + num);
            sign.setLine(1, p.getDisplayName());
            sign.update();
        });
   
        // CREATE CONNECTOR PINS AND WIRE
        for (int i = 0; i <= sidelength; i += 2)
        {
            context.addBlock(x + i, y, z - 1, Material.IRON_BLOCK); // NORTH SIDE
            context.addBlock(x + i, y, z + sidelength, Material.IRON_BLOCK); // SOUTH SIDE
            context.addBlock(x - 1, y, z + i, Material.IRON_BLOCK); // WEST SIDE
            context.addBlock(x + sidelength, y, z + i, Material.IRON_BLOCK); // EAST SIDE
            
            context.addBlock(x + i, y, z - 2, Material.REDSTONE_WIRE); // NORTH SIDE
            context.addBlock(x + i, y, z + sidelength + 1, Material.REDSTONE_WIRE); // SOUTH SIDE
            context.addBlock(x - 2, y, z + i, Material.REDSTONE_WIRE); // WEST SIDE
            context.addBlock(x + sidelength + 1, y, z + i, Material.REDSTONE_WIRE); // EAST SIDE
        }

        return createProcessor(context, caller, p, x - 1, y, z - 1, iosidecount);
    }

    public static final <T extends SquareEmulatedProcessor> void registerFactory(String archname, SquareEmulatedProcessorFactory<T> fac) throws Exception
    {
        ComponentFactory.registerFactory("processor.emulated." + archname, fac);
    }
    
    public static final SquareEmulatedProcessor createProcessor(String archname, BlockPlacingContext context, MCPUCore caller, Player p, Location l, ComponentOrientation or, int iocount)
            throws ClassNotFoundException, InvalidOrientationException
    {
        return (SquareEmulatedProcessor)getFactoryByName("processor.emulated." + archname).spawnComponent(context, caller, p, l.getBlockX(), l.getBlockY(), l.getBlockZ(), or, iocount);
    }    
}
