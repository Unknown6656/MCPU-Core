package epsilonpotato.mcpu.util;

import java.io.Serializable;

public interface TriFunction<T, U, V, W> extends Serializable
{
    static final long serialVersionUID = 0x0e0bab24fb0a88cdL;
    
    
    public W eval(T t, U u, V v);
}
