package epsilonpotato.mcpu.util;

import java.io.Serializable;

/**
 * Represents a generic function of the type <pre>{@link T} * {@link U} -> {@link V}</pre>
 * @param <T> Generic parameter type {@link T}
 * @param <U> Generic parameter type {@link U}
 * @param <V> Generic result type {@link V}
 * @author Unknown6656
 */
public interface BiFunction<T, U, V> extends Serializable
{
    /**
     * The serialisation unique ID
     */
    static final long serialVersionUID = 0xe80cd9f8d4dfa784L;
    
    
    /**
     * The evaluates the function with the given parameters
     * @param t Parameter {@link T}
     * @param u Parameter {@link U}
     * @return Result value {@link V}
     */
    public V eval(T t, U u);
    
    /**
     * The evaluates the function with the given parameter tuple ({@link T}, {@link U})
     * @param t Parameter tuple ({@link T}, {@link U})
     * @return Return value {@link V}
     */
    public default V eval(Tuple<T, U> t)
    {
        return eval(t.x, t.y);
    }
    
    /**
     * The evaluates the function curried with the given parameter {@link T}
     * @param t Parameter {@link T}
     * @return Curried function <pre>{@link U} -> {@link V}</pre>
     */
    public default Function<U, V> eval(T t)
    {
        return (u -> eval(t, u));
    }
}
