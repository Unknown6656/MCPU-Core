package epsilonpotato.mcpu.util;

import java.io.Serializable;

/**
 * Represents a generic function of the type 'T -> U'
 * @param <T> Generic type T
 * @param <U> Generic type U
 * @author Unknown6656
 */
public interface Function<T, U> extends Serializable
{
    /**
     * The serialisation unique ID
     */
    static final long serialVersionUID = 0x6315497a88a4bb0eL;
    
    
    /**
     * The function invocation
     * @param t Parameter T
     * @return Return value U
     */
    public U eval(T t);
}
