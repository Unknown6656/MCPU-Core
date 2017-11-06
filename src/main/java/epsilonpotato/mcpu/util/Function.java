package epsilonpotato.mcpu.util;

import java.io.Serializable;

/**
 * Represents a generic function of the type <pre>{@link T} -> {@link U}</pre>
 * @param <T> Generic parameter type {@link T}
 * @param <U> Generic return type {@link U}
 * @author Unknown6656
 */
public interface Function<T, U> extends Serializable
{
    /**
     * The serialisation unique ID
     */
    static final long serialVersionUID = 0x6315497a88a4bb0eL;
    
    
    /**
     * The evaluates the function with the given parameter
     * @param t Parameter {@link T}
     * @return Return value {@link U}
     */
    public U eval(T t);
}
