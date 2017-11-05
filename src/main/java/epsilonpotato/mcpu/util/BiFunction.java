package epsilonpotato.mcpu.util;

import java.io.Serializable;

/**
 * Represents a generic function of the type 'T * U -> V'
 * @param <T> Generic type T
 * @param <U> Generic type U
 * @param <V> Generic type V
 * @author Unknown6656
 */
public interface BiFunction<T, U, V> extends Serializable
{
    /**
     * The serialisation unique ID
     */
    static final long serialVersionUID = 0xe80cd9f8d4dfa784L;
    
    
    public V eval(T t, U u);
    
    public default V eval(Tuple<T, U> t)
    {
        return eval(t.x, t.y);
    }
    
    public default Function<U, V> eval(T t)
    {
        return (u -> eval(t, u));
    }
}
