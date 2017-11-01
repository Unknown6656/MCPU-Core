package epsilonpotato.mcpu.util;

import java.io.Serializable;

public interface BiFunction<T, U, V> extends Serializable
{
    static final long serialVersionUID = 0xe80cd9f8d4dfa784L;
    
    
    public V eval(T t, U u);
}
