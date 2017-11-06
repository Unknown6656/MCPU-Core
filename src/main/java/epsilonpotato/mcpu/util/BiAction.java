package epsilonpotato.mcpu.util;

import java.io.Serializable;

/**
 * Represents a generic function of the type <pre>{@link T} * {@link U} -> ()</pre>
 * @param <T> Generic parameter type {@link T}
 * @param <U> Generic parameter type {@link U}
 * @author Unknown6656
 */
public interface BiAction<T, U> extends Serializable
{
    /**
     * The serialisation unique ID
     */
    static final long serialVersionUID = 0x172453d0b3e7fde3L;
    
    
    /**
     * The evaluates the function with the given parameters
     * @param t Parameter {@link T}
     * @param u Parameter {@link U}
     */
    public void eval(T t, U u);
    
    /**
     * The evaluates the function with the given parameter tuple ({@link T}, {@link U})
     * @param t Parameter tuple ({@link T}, {@link U})
     */
    public default void eval(Tuple<T, U> t)
    {
        eval(t.x, t.y);
    }
    
    /**
     * The evaluates the function curried with the given parameter {@link T}
     * @param t Parameter {@link T}
     * @return Curried function <pre>{@link U} -> ()</pre>
     */
    public default Action<U> eval(T t)
    {
        return (u -> eval(t, u));
    }
}
