package epsilonpotato.mcpu.core;

import java.io.Serializable;

/**
 * Represents an I/O port
 * @author Unknown6656
 */
public final class IOPort implements Serializable
{
    private static final long serialVersionUID = -3510786531367958282L;
    private boolean direction;
    private byte value;
    
    
    /**
     * Creates a new instance
     * @param v Initial port value (positive integer between inclusive 0 and 15)
     * @param d Initial port direction (true := out, false := in)
     */
    public IOPort(int v, boolean d)
    {
        setValue((byte)v);
        setDirection(d);
    }

    /**
     * Returns the port's current value
     * @return Port value
     */
    public byte getValue()
    {
        return value;
    }

    /**
     * Sets the port's current value to the new given one
     * @param value New port value
     */
    public void setValue(int value)
    {
        this.value = (byte)Math.max(0, Math.min(value, 15));
    }

    /**
     * Returns the port's current direction
     * @return Port direction (true := out, false := in)
     */
    public boolean getDirection()
    {
        return direction;
    }

    /**
     * Sets the port's current direction to the new given one
     * @param direction New port direction (true := out, false := in)
     */
    public void setDirection(boolean direction)
    {
        this.direction = direction;
    }

    /**
     * Returns whether the port is currently 'low' (the value == 0)
     * @return The port's low state
     */
    public boolean isLow()
    {
        return getValue() == 0;
    }
}
