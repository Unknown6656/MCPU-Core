package epsilonpotato.mcpu.util;

import java.io.Serializable;

public interface Action<T> extends Serializable
{
    static final long serialVersionUID = 0x6dc8e64736cf9b29L;
    
    
    public void eval(T t);
}
