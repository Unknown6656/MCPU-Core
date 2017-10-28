package epsilonpotato.mcpu.core;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public final class BlockPlacingContext
{
    private final ArrayList<BlockState> oldblocks = new ArrayList<>();
    private final ArrayList<Block> addedblocks = new ArrayList<>();
    private final World world;
    
    
    public BlockPlacingContext(World w)
    {
        world = w;
    }
    
    public Block addBlock(int x, int y, int z, Material m)
    {
        Block b = new Location(world, x, y, z).getBlock();
     
        oldblocks.add(b.getState());
        b.setType(m);
        addedblocks.add(b);
        
        return b;
    }
    
    public void addBlock(int x, int y, int z, Material m, Consumer<Block> f)
    {
        f.accept(addBlock(x, y, z, m));
    }
    
    public void rollback()
    {
        Block[] copy = addedblocks.toArray(new Block[addedblocks.size()]);
        
        for (BlockState bs : oldblocks)
        {   
            Location oldpos = bs.getLocation();
            
            for (int i = 0; i < copy.length; ++i)
                if (copy[i] != null)
                {
                    Location newpos = copy[i].getLocation(); 
                    
                    if ((oldpos.getBlockX() == newpos.getBlockX()) &&
                        (oldpos.getBlockY() == newpos.getBlockY()) &&
                        (oldpos.getBlockZ() == newpos.getBlockZ()))
                    {
                        // Block target = world.getBlockAt(oldpos);
                        //  target.setType(bs.getType());

                        bs.update();
                        
                        copy[i] = null;
                        
                        break;
                    }
                }
        }
    }
    
    public World getWorld()
    {
        return world;
    }
    
    public ArrayList<Block> getBlocks()
    {
        return addedblocks;
    }
}
