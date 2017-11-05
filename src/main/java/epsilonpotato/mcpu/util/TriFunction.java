package epsilonpotato.mcpu.util;

import java.io.Serializable;

/**
 * Represents a generic function of the type 'T * U * V -> W'
 * @param <T> Generic type T
 * @param <U> Generic type U
 * @param <V> Generic type V
 * @param <W> Generic type W
 * @author Unknown6656
 */
public interface TriFunction<T, U, V, W> extends Serializable
{
    /**
     * The serialisation unique ID
     */
    static final long serialVersionUID = 0x0e0bab24fb0a88cdL;
    
    
    public W eval(T t, U u, V v);

    public default W eval(Triplet<T, U, V> t)
    {
        return eval(t.x, t.y, t.z);
    }

    public default Function<V, W> eval(Tuple<T, U> t)
    {
        return eval(t.x, t.y);
    }
    
    public default Function<V, W> eval(T t, U u)
    {
        return v -> eval(t, u, v);
    }
    
    public default BiFunction<U, V, W> eval(T t)
    {
        return (u, v) -> eval(t, u, v);
    }
}
