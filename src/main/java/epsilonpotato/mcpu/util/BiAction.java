package epsilonpotato.mcpu.util;

import java.io.Serializable;

/**
 * Represents a generic function of the type 'T * U -> ()'
 * @param <T> Generic type T
 * @param <U> Generic type U
 * @author Unknown6656
 */
public interface BiAction<T, U> extends Serializable
{
    /**
     * The serialisation unique ID
     */
    static final long serialVersionUID = 0x172453d0b3e7fde3L;
    
    
    public void eval(T t, U u);
    
    public default void eval(Tuple<T, U> t)
    {
        eval(t.x, t.y);
    }
    
    public default Action<U> eval(T t)
    {
        return (u -> eval(t, u));
    }
}
