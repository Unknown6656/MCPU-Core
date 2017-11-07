package epsilonpotato.mcpu.util;

import java.io.Serializable;

/**
 * Represents a triplet composed of three generic values
 * @param <X> First component generic type
 * @param <Y> Second component generic type
 * @param <Z> Third component generic type
 * @author Unknown6656
 */
public final class Triplet<X, Y, Z> implements Serializable
{
    private static final long serialVersionUID = -8100911397280809156L;
    /**
     * First triplet component
     */
    public X x;
    /**
     * Second triplet component
     */
    public Y y;
    /**
     * Third triplet component
     */
    public Z z;
    
    
    /**
     * Creates a new triple with the three given values
     * @param x First triplet component
     * @param y Second triplet component
     * @param z Third triplet component
     */
    public Triplet(X x, Y y, Z z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
