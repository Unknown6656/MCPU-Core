package epsilonpotato.mcpu.core;

import java.net.URI;

import org.bukkit.Location;
import org.bukkit.entity.Player;


public class SevenSegmentDisplay extends EmulatedProcessor
{

    public SevenSegmentDisplay(Player p, Location l, Triplet<Integer, Integer, Integer> size, int iocount)
    {
        super(p, l, size, iocount);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void nextInstruction()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void reset()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void start()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void stop()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean load(URI source)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public byte getIO(int port)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setIO(int port, byte value)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Location getIOLocation(int port)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setIODirection(int port, boolean direction)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean getIODirection(int port)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getState()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getTicksElapsed()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getIOCount()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
}
