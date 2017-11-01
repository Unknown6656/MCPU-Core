package epsilonpotato.mcpu.util;

import java.io.Serializable;

public interface Function<T, U> extends Serializable
{
    static final long serialVersionUID = 0x6315497a88a4bb0eL;
    
    
    public U eval(T t);
}
