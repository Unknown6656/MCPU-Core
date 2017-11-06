package epsilonpotato.mcpu.util;

import java.io.Serializable;

/**
 * Represents a tuple composed of two generic values
 * @param <X> First component generic type
 * @param <Y> Second component generic type
 * @author Unknown6656
 */
public final class Tuple<X, Y> implements Serializable
{
    private static final long serialVersionUID = 3359005246070190473L;
    /**
     * First tuple component
     */
    public X x;
    /**
     * Second tuple component
     */
    public Y y;
    
    
    /**
     * Creates a new tuple with the two given values
     * @param x First tuple component
     * @param y Second tuple component
     */
    public Tuple(X x, Y y)
    {
        this.x = x;
        this.y = y;
    }
}
