package epsilonpotato.mcpu.util;

import java.io.Serializable;

/**
 * Represents a generic function of the type 'T -> ()'
 * @param <T> Generic type T
 * @author Unknown6656
 */
public interface Action<T> extends Serializable
{
    /**
     * The serialisation unique ID
     */
    static final long serialVersionUID = 0x6dc8e64736cf9b29L;
    
    
    /**
     * @param t
     */
    public void eval(T t);
}
