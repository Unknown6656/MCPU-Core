package epsilonpotato.mcpu.util;

import java.io.Serializable;

/**
 * Represents a generic function of the type <pre>{@link T} -> ()</pre>
 * @param <T> Generic parameter type {@link T}
 * @author Unknown6656
 */
public interface Action<T> extends Serializable
{
    /**
     * The serialisation unique ID
     */
    static final long serialVersionUID = 0x6dc8e64736cf9b29L;
    
    
    /**
     * The evaluates the action with the given parameter
     * @param t Parameter {@link T}
     */
    public void eval(T t);
}
