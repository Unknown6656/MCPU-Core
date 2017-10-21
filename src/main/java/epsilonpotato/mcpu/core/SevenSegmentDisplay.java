package epsilonpotato.mcpu.core;

import java.net.URI;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;


public final class SevenSegmentDisplay extends EmulatedProcessor
{
    /*
     *  13  0  8
     *    .---.
     *  5 |   | 1
     * 12 >-6-< 9
     *  4 |   | 2
     *    '---' [7]
     *  11  3  10
     *  
     *  
     *  8 = 0 | 1
     *  9 = 1 | 2 | 6
     * 10 = 2 | 3
     * 11 = 3 | 4
     * 12 = 4 | 5 | 6
     * 13 = 0 | 5
     * 
     */
    private static final short[] SegmentMap = new short[] { 0xfc, 0x60, 0xda, 0xf2, 0x66, 0xb6, 0xbe, 0xd0, 0xfe, 0xf6, 0xee, 0x3e, 0x1a, 0x7a, 0x9e, 0x8e };
    private static final byte[][] PixelMap = new byte[][]
    {
        { 20, 21, 22 }, // 0
        { 32, 41, 50 }, // 1
        { 68, 77, 86 }, // 2
        { 92, 93, 94 }, // 3
        { 64, 73, 82 }, // 4
        { 28, 37, 46 }, // 5
        { 56, 57, 58 }, // 6
        { 97 }, // 7
        { 23 }, // 8
        { 59 }, // 9
        { 95 }, // 10
        { 91 }, // 11
        { 55 }, // 12
        { 19 }, // 13
    };
    private static final Material materialOn = Material.REDSTONE_BLOCK;
    private static final Material materialOff = Material.STONE;
    private byte ioval = 0x00;
    
    
    public SevenSegmentDisplay(Player p, int x, int y, int z)
    {
        super(p, new Location(p.getWorld(), x, y, z), new Triplet<>(9, 2, 12), 5);
    }

    @Override
    public void nextInstruction()
    {
    }

    public boolean IsEnabled()
    {
        Block b = world.getBlockAt(x, y + 1, z);
        
        return b.getType() == Material.LEVER ? b.isBlockPowered() || (b.getBlockPower() != 0) : true;
    }

    private void setDisplay(byte value)
    {
        ioval = value;
        
        if (!IsEnabled())
            return;
        
        short segments = SegmentMap[ioval & 0x0f];
        boolean[] onstate = new boolean[7];
        
        for (int i = 7; i >= 1; --i)
        {
            boolean on = ((segments >> i) & 1) != 0;
            
            for (byte loc : PixelMap[7 - i])
                setPixelAt(loc, on);
            
            onstate[7 - i] = on;
        }

        // SET DOT
        setPixelAt(PixelMap[7][0], (ioval & 0x10) != 0);
        
        // // SET CORNER PIXELS
        // setPixelAt(PixelMap[8][0], onstate[0] | onstate[1]);
        // setPixelAt(PixelMap[9][0], onstate[1] | onstate[2] | onstate[6]);
        // setPixelAt(PixelMap[10][0], onstate[2] | onstate[3]);
        // setPixelAt(PixelMap[11][0], onstate[3] | onstate[4]);
        // setPixelAt(PixelMap[12][0], onstate[4] | onstate[5] | onstate[6]);
        // setPixelAt(PixelMap[13][0], onstate[0] | onstate[5]);
    }
    
    private void setPixelAt(int loc, boolean on)
    {
        int xoffs = loc % 9;
        int zoffs = loc / 9;
        Block b = world.getBlockAt(x + xoffs, y, z + zoffs);
        
        b.setType(on ? materialOn : materialOff);
        b.getState().update();
    }
    
    @Override
    public void reset()
    {
        setDisplay((byte)0);
    }

    @Override
    public void start()
    {
    }

    @Override
    public void stop()
    {
    }

    @Override
    public boolean load(URI source)
    {
        return false;
    }

    @Override
    public byte getIO(int port)
    {
        return 0;
    }

    @Override
    public void setIO(int port, byte value)
    {
        if (port < getIOCount())
        {
            byte newval = (byte)(ioval & ~(1 << port));
            
            setDisplay((byte)(newval | (value > 0 ? 1 << port : 0)));   
        }
    }

    @Override
    public Location getIOLocation(int port)
    {
        return new Location(world, x + 2 * (4 - port), y, z + 12);
    }

    @Override
    public void setIODirection(int port, boolean direction)
    {
    }

    @Override
    public boolean getIODirection(int port)
    {
        return false;
    }

    @Override
    public String getState()
    {
        return String.format("%02x", ioval);
    }

    @Override
    public long getTicksElapsed()
    {
        return 0;
    }

    @Override
    public int getIOCount()
    {
        return 5;
    }
}
