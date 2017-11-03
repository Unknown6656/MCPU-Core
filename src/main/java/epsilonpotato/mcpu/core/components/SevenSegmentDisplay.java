package epsilonpotato.mcpu.core.components;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import epsilonpotato.mcpu.core.ComponentOrientation;
import epsilonpotato.mcpu.core.IntegratedCircuit;
import epsilonpotato.mcpu.core.InvalidOrientationException;
import epsilonpotato.mcpu.util.Triplet;
import epsilonpotato.mcpu.util.YamlConfiguration;


public final class SevenSegmentDisplay extends IntegratedCircuit
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
     */
    private static final long serialVersionUID = 1347313568910882948L;
    private static final short[] SegmentMap = new short[] { 0xfc, 0x60, 0xda, 0xf2, 0x66, 0xb6, 0xbe, 0xe0, 0xfe, 0xf6, 0xee, 0x3e, 0x9c, 0x7a, 0x9e, 0x8e };
    private static final byte[][] PixelMap = new byte[][]
    {
        { 11, 12, 13 }, // 0
        { 23, 32, 41 }, // 1
        { 59, 68, 77 }, // 2
        { 83, 84, 85 }, // 3
        { 55, 64, 73 }, // 4
        { 19, 28, 37 }, // 5
        { 47, 48, 49 }, // 6
        { 88 }, // 7
        { 14 }, // 8
        { 50 }, // 9
        { 86 }, // 10
        { 82 }, // 11
        { 46 }, // 12
        { 10 }, // 13
    };
    private static final Material materialOn = Material.REDSTONE_BLOCK;
    private static final Material materialOff = Material.NETHER_BRICK;
    private byte ioval = 0x00;
    
    
    public SevenSegmentDisplay(Player p, World w, int x, int y, int z) throws InvalidOrientationException
    {
        super(p, new Location(w, x, y, z), new Triplet<>(9, 2, 12), 5, ComponentOrientation.NORTH);
    }

    private void updateDisplay()
    {   
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
    public Location getIOLocation(int port)
    {
        return new Location(world, x + 2 * port, y, z + 12);
    }

    @Override
    public String getState()
    {
        return String.format("%02x", ioval);
    }

    @Override
    protected void onTick()
    {
        ioval = 0;
        
        for (int i = 0; i < 5; ++i)
            ioval |= (io[i == 4 ? 4 : 3 - i].getValue() > 0 ? 1 << i : 0);

        updateDisplay();
    }

    @Override
    protected ComponentOrientation[] getValidOrientations()
    {
        return new ComponentOrientation[] { ComponentOrientation.NORTH };
    }

    @Override
    protected final void serializeComponentSpecific(YamlConfiguration conf)
    {
        conf.set("ioval", (int)ioval);
    }

    @Override
    protected void deserializeComponentSpecific(YamlConfiguration conf)
    {
        ioval = (byte)conf.getInt("ioval", 0);
    }
}
