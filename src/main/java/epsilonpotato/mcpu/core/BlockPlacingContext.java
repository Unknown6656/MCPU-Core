package epsilonpotato.mcpu.core;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import epsilonpotato.mcpu.util.Action;

/**
 * Represents a context for the placement of blocks inside a world with rollback-support
 * @author Unknown6656
 */
public final class BlockPlacingContext
{
    private final ArrayList<BlockState> oldblocks = new ArrayList<>();
    private final ArrayList<Block> addedblocks = new ArrayList<>();
    private final World world;
    
    
    /**
     * Creates a new instance
     * @param w World
     */
    public BlockPlacingContext(World w)
    {
        world = w;
    }
    
    /**
     * Adds a new block to the world and to the internal rollback-list
     * @param x The block's target X-coordinate
     * @param y The block's target Y-coordinate
     * @param z The block's target Z-coordinate
     * @param m The block's new material
     * @return The added block
     */
    public Block addBlock(int x, int y, int z, Material m)
    {
        Block b = new Location(world, x, y, z).getBlock();
     
        oldblocks.add(b.getState());
        b.setType(m);
        addedblocks.add(b);
        
        return b;
    }
    
    /**
     * Adds a new block to the world and to the internal rollback-list
     * @param x The block's target X-coordinate
     * @param y The block's target Y-coordinate
     * @param z The block's target Z-coordinate
     * @param m The block's new material
     * @param f A callback function with the added block as its parameter
     */
    public void addBlock(int x, int y, int z, Material m, Action<Block> f)
    {
        f.eval(addBlock(x, y, z, m));
    }

    /**
     * Adds a new block to the world and to the internal rollback-list
     * @param loc The block's target location
     * @param m The block's new material
     * @return The added block
     */
    public Block addBlock(Location loc, Material m)
    {
        return addBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), m);
    }

    /**
     * Adds a new block to the world and to the internal rollback-list
     * @param loc The block's target location
     * @param m The block's new material
     * @param f A callback function with the added block as its parameter
     */
    public void addBlock(Location loc, Material m, Action<Block> f)
    {
        f.eval(addBlock(loc, m));   
    }
    
    /**
     * Performs a rollback-operation. This removes all added blocks from the internal list and resets the world to its original state at the corresponding block locations.
     */
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
        
        addedblocks.clear();
    }
    
    /**
     * Returns the world to which the current block placing context is associated
     * @return World
     */
    public World getWorld()
    {
        return world;
    }
    
    /**
     * Returns a list of the added blocks
     * @return List of added blocks
     */
    public ArrayList<Block> getBlocks()
    {
        return addedblocks;
    }
}
