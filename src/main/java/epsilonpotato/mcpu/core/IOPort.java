package epsilonpotato.mcpu.core;


public final class IOPort
{
    private boolean direction;
    private byte value;
    
    
    public IOPort(int v, boolean d)
    {
        setValue((byte)v);
        setDirection(d);
    }

    public byte getValue()
    {
        return value;
    }

    public void setValue(int value)
    {
        this.value = (byte)Math.max(0, Math.min(value, 15));
    }

    public boolean getDirection()
    {
        return direction;
    }

    public void setDirection(boolean direction)
    {
        this.direction = direction;
    }
}
