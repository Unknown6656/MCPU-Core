package epsilonpotato.mcpu.core;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import static epsilonpotato.mcpu.core.MCPUCore.*;


public abstract class SquareEmulatedProcessorFactory<T extends SquareEmulatedProcessor> extends ComponentFactory<SquareEmulatedProcessor>
{
    public abstract T createProcessor(MCPUCore caller, Player p, World w, int x, int y, int z, int iosidecount);
    
    
    @Override
    @SuppressWarnings("deprecation")
    public final SquareEmulatedProcessor spawnComponent(MCPUCore caller, Player p, World w, int x, int y, int z, ComponentOrientation or, int iosidecount)
            throws InvalidOrientationException
    {
        if (or != ComponentOrientation.NORTH)
            throw new InvalidOrientationException(or, ComponentOrientation.NORTH);
        
        int sidelength = iosidecount * 2 - 1;
        
        x += 1;
        z += 1;
        
        deleteRegion(w, x - 2, y - 1, z - 2, sidelength + 4, 3, sidelength + 4);
        
        // CREATE STONE BASE
        for (int i = -2; i < sidelength + 2; ++i)
            for (int j = -2; j < sidelength + 2; ++j)
                SetBlock(w, x + i, y - 1, z + j, Material.STONE);
        
        // CREATE WOOL BODY
        for (int i = 0; i < sidelength; ++i)
            for (int j = 0; j < sidelength; ++j)
                SetBlock(w, x + i, y, z + j, Material.WOOL, b -> b.setData(DyeColor.BLACK.getWoolData())); // TODO: fix deprecated calls
        
        // CREATE GOLD CORNER + SIGN + LEVER
        int num = circuits.size();
        
        SetBlock(w, x, y, z, Material.GOLD_BLOCK);
        SetBlock(w, x + 1, y, z, Material.GOLD_BLOCK);
        SetBlock(w, x + 1, y + 1, z, Material.LEVER, b ->
        {
            b.setData((byte)6);
            b.getState().update();
        });
        SetBlock(w, x, y + 1, z, Material.SIGN_POST, b ->
        {
            Sign sign = (Sign)b.getState();
            
            sign.setLine(0, "CPU No. " + num);
            sign.setLine(1, p.getDisplayName());
            sign.update();
        });
   
        // CREATE CONNECTOR PINS AND WIRE
        for (int i = 0; i <= sidelength; i += 2)
        {
            SetBlock(w, x + i, y, z - 1, Material.IRON_BLOCK); // NORTH SIDE
            SetBlock(w, x + i, y, z + sidelength, Material.IRON_BLOCK); // SOUTH SIDE
            SetBlock(w, x - 1, y, z + i, Material.IRON_BLOCK); // WEST SIDE
            SetBlock(w, x + sidelength, y, z + i, Material.IRON_BLOCK); // EAST SIDE
            
            SetBlock(w, x + i, y, z - 2, Material.REDSTONE_WIRE); // NORTH SIDE
            SetBlock(w, x + i, y, z + sidelength + 1, Material.REDSTONE_WIRE); // SOUTH SIDE
            SetBlock(w, x - 2, y, z + i, Material.REDSTONE_WIRE); // WEST SIDE
            SetBlock(w, x + sidelength + 1, y, z + i, Material.REDSTONE_WIRE); // EAST SIDE
        }

        return createProcessor(caller, p, w, x - 1, y, z - 1, iosidecount);
    }

    public static final <T extends SquareEmulatedProcessor> void registerFactory(String archname, SquareEmulatedProcessorFactory<T> fac) throws Exception
    {
        ComponentFactory.registerFactory("processor.emulated." + archname, fac);
    }
    
    public static final SquareEmulatedProcessor createProcessor(String archname, MCPUCore caller, Player p, Location l, ComponentOrientation or, int iocount)
            throws ClassNotFoundException, InvalidOrientationException
    {
        return (SquareEmulatedProcessor)getFactoryByName("processor.emulated." + archname).spawnComponent(caller, p, l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), or, iocount);
    }    
}
