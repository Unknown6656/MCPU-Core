package epsilonpotato.mcpu.util;

import java.io.Serializable;

/**
 * Represents a generic function of the type <pre>{@link T} * {@link U} * {@link V} -> {@link W}</pre>
 * @param <T> Generic parameter type {@link T}
 * @param <U> Generic parameter type {@link U}
 * @param <V> Generic parameter type {@link V}
 * @param <W> Generic return type {@link W}
 * @author Unknown6656
 */
public interface TriFunction<T, U, V, W> extends Serializable
{
    /**
     * The serialisation unique ID
     */
    static final long serialVersionUID = 0x0e0bab24fb0a88cdL;
    
    
    /**
     * The evaluates the function with the given parameters
     * @param t Parameter {@link T}
     * @param u Parameter {@link U}
     * @param v Parameter {@link V}
     * @return Return value {@link W}
     */
    public W eval(T t, U u, V v);

    /**
     * The evaluates the function with the given parameters
     * @param t Parameter triplet ({@link T}, {@link U}, {@link V})
     * @return Return value {@link W}
     */
    public default W eval(Triplet<T, U, V> t)
    {
        return eval(t.x, t.y, t.z);
    }

    /**
     * The evaluates the function curried with the given parameter tuple ({@link T}, {@link U})
     * @param t Parameter tuple ({@link T}, {@link U})
     * @return Curried function <pre>{@link V} -> {@link W}</pre>
     */
    public default Function<V, W> eval(Tuple<T, U> t)
    {
        return eval(t.x, t.y);
    }
    
    /**
     * The evaluates the function curried with the given parameters {@link T} and {@link U}
     * @param t Parameter {@link T}
     * @param u Parameter {@link U}
     * @return Curried function <pre>{@link V} -> {@link W}</pre>
     */
    public default Function<V, W> eval(T t, U u)
    {
        return v -> eval(t, u, v);
    }
    
    /**
     * The evaluates the function curried with the given parameter {@link T}
     * @param t Parameter {@link T}
     * @return Curried function <pre>({@link U} * {@link V}) -> {@link W}</pre>
     */
    public default BiFunction<U, V, W> eval(T t)
    {
        return (u, v) -> eval(t, u, v);
    }
}
