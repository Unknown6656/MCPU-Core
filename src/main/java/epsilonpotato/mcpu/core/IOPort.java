package epsilonpotato.mcpu.core;

import java.io.Serializable;

public final class IOPort implements Serializable
{
    private static final long serialVersionUID = -3510786531367958282L;
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

    public boolean isLow()
    {
        return getValue() == 0;
    }
}
